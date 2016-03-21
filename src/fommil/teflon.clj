(ns fommil.teflon
  (:require
   [clojure.string :as string]
   [fommil.oauth :as oauth]
   [org.httpkit.client :as http]
   [clojure.core.async :as async :refer :all
    :exclude [map into reduce merge partition partition-by take]]
   [cheshire.core :as json])
  (:import
   [fommil.oauth Consumer]))

(load-file "config.clj")

(defn heartbeat
  ;; by Paul Butcher
  "Return a channel that pings every interval ms"
  [interval]
  (let [ch (chan)]
    (go (while (>! ch :ping)
          (<! (timeout interval))))
    ch))

(defn rate-limited
  ;; by Paul Butcher
  "Return a channel that's rate-limited to an average of one item per interval"
  [in interval]
  (let [out (chan)
        hb (heartbeat interval)]
    (go (while (>! out (<! in))
          (<! hb)))
    out))

(defn twitter-request
  "Wraps `http/request' with OAuth1.0 authentication assuming that the
  consumer and access tokens are defined. Returns a channel."
  [method endpoint query-params]
  (let [ch (chan)
        url (string/join "" ["https://api.twitter.com/" endpoint])
        consumer (oauth/Consumer.
                  +twitter_consumer_key+
                  +twitter_consumer_secret+
                  "https://api.twitter.com/oauth/request_token"
                  "https://api.twitter.com/oauth/access_token"
                  "https://api.twitter.com/oauth/authorize"
                  :hmac-sha1)
        credentials (oauth/credentials
                     consumer
                     +twitter_access_token+
                     +twitter_access_token_secret+
                     method
                     url
                     query-params)
        payload (merge credentials query-params)]
    (http/request
     {:method method :url url :query-params payload}
     (fn [response]
       (if (= 200 (:status response))
         (put! ch response)
         (do (close! ch)
             (throw (Exception. response))))))
    ch))

(defn get-tweets-batch
  "Return a channel containing a batch of tweets (id strictly less than
  the max_id) for the given user."
  [user max_id]
  (let [in (->>
            (if max_id {:max_id (dec max_id)} {})
            ;; 200 is maximum
            (merge {:screen_name user :count 200})
            (twitter-request
             :get "1.1/statuses/user_timeline.json"))
        out (chan)]
    (go
      (if-let [resp (<! in)]
        (let [json (:body resp)
              parsed (json/parse-string json true)]
          (>! out parsed))
        (close! out)))
    out))

(defn get-tweets-all
  "Return a channel containing tweets for the `user', newest first."
  [user]
  (let [out (chan)]
    (go-loop [previous nil]
      (if-let [tweets (<! (get-tweets-batch user previous))]
        (do
          (>! out tweets)
          (recur (:id (last tweets))))
        (close! out)))
    out))

(defn delete-tweet?
  "True if this tweet is not good enough to keep."
  [tweet]
  (let [{retweets :retweet_count
         likes :favorite_count} tweet]
    (and (< retweets 5) (< likes 5))))

(defn -main []
  (println "hello")
  ;; https://dev.twitter.com/rest/public/rate-limits
  ;; statuses/user_timeline limited to 300 per 15 minutes
  (def tweets (rate-limited (get-tweets-all +twitter_username+) 3000))

  (loop []
    (when-let [batch (<!! tweets)]
      (println "GOT" (count batch) "tweets" (map :id batch))
      (recur)))

  (println "goodbye")
  ;;(println (map :text (filter delete-tweet? ex)))
  )


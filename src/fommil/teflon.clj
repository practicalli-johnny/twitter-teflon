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
  "Get a channel with tweets for the given user before the given id."
  [user max_id]
  (let [in (->>
            (if max_id {:max_id max_id} {})
            (merge {:screen_name user})
            (twitter-request
             :get "1.1/statuses/user_timeline.json"))
        out (chan)]
    (go
      (when-let [resp (<! in)]
        (let [json (:body resp)
              parsed (json/parse-string json true)]
          (loop [remaining parsed]
            (when (seq remaining)
              ;; there has got to be a convenience for putting a sequence onto a channel
              ;; or should we just return the whole batch?
              (>! out (first remaining))
              (recur (rest remaining)))))
        (close! out)))
    out))

;; (defn all-tweets
;;   ;; would be better to use backpressure with work,
;;   ;; this is super slow and blocking, and
;;   ;; exceeds the rate limits.
;;   "Get all tweets for the user, ending at max_id."
;;   ([user] (all-tweets user nil nil))
;;   ([user max_id acc]
;;    (let [batch (get-tweets user max_id)]
;;      (if (empty? batch)
;;        acc
;;        (let [rev (reverse batch)
;;              oldest (:id (first rev))]
;;          (recur user (dec oldest) (concat rev acc)))))))

(defn delete-tweet?
  "True if this tweet is not good enough to keep."
  [tweet]
  (let [{retweets :retweet_count
         likes :favorite_count} tweet]
    (and (< retweets 5) (< likes 5))))

(defn -main []
  (println "hello")
  (def tweets (get-tweets-batch +twitter_username+ nil))

  (loop []
    (when-let [tweet (<!! tweets)]
      (println (:text tweet))
      (recur)))

  (println "goodbye")
  ;;(println (map :text (filter delete-tweet? ex)))
  )


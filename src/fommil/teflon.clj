(ns fommil.teflon
  (:require
   [clojure.string :as string]
   [fommil.oauth :as oauth]
   [org.httpkit.client :as http]
   [cheshire.core :as json])
  (:import
   [fommil.oauth Consumer]))

(load-file "config.clj")

(defn twitter-request
  "Wraps `http/request' with OAuth1.0 authentication assuming that the
  consumer and access tokens are defined."
  [method endpoint query-params]
  (let [url (string/join "" ["https://api.twitter.com/" endpoint])
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
                     query-params)]
    (:body
     ;; transitional approach, will go async soon
     @(http/request
       {:method method
        :url url
        :query-params (merge credentials query-params)}))))

(defn tweets
  "Get tweets for the given user before the given id.
  A tweet contains: `id', `text', `retweet_count', `favorite_count'."
  [user max_id]
  (as-> max_id <>
        (if max_id {:max_id max_id} {})
        (merge {:screen_name user} <>)
        (twitter-request
         :get "1.1/statuses/user_timeline.json" <>)
        (json/parse-string <> true)))

(defn all-tweets
  ;; would be better to use backpressure with work,
  ;; this is super slow and blocking, and
  ;; exceeds the rate limits.
  "Get all tweets for the user, ending at max_id."
  ([user] (all-tweets user nil nil))
  ([user max_id acc]
   (let [batch (tweets user max_id)]
     (if (empty? batch)
       acc
       (let [rev (reverse batch)
             oldest (:id (first rev))]
         (recur user (dec oldest) (concat rev acc)))))))

(defn delete-tweet?
  "True if this tweet is not good enough to keep."
  [tweet]
  (let [{retweets :retweet_count
         likes :favorite_count} tweet]
    (and (< retweets 5) (< likes 5))))

(defn -main []
  (def ex (tweets +twitter_username+ nil))
  (println (map :text (filter delete-tweet? ex))))


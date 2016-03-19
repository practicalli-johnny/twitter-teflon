(ns fommil.teflon.core
  (:require
   [clojure.string :as string]
   [oauth.client :as oauth]
   [clj-http.client :as http]))

(load-file "config.clj")

(defn twitter-request
  "Wraps `http/request' with OAuth1.0 authentication assuming that the
  consumer and access tokens are defined."
  [method endpoint query-params]
  (let [url (string/join "" ["https://api.twitter.com/" endpoint])
        consumer (oauth/make-consumer
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
    (http/request
     {:method method
      :url url
      :query-params (merge credentials query-params)})))

(defn -main []
  (println (twitter-request
            :get "1.1/statuses/user_timeline.json"
            {:screen_name "fommil"})))


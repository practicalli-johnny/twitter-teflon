(ns fommil.teflon.core
  (:require
   [oauth.client :as oauth]
   [clj-http.client :as http]))

(load-file "config.clj")

(def consumer (oauth/make-consumer
               +twitter_consumer_key+
               +twitter_consumer_secret+
               "https://api.twitter.com/oauth/request_token"
               "https://api.twitter.com/oauth/access_token"
               "https://api.twitter.com/oauth/authorize"
               :hmac-sha1))

(def credentials (oauth/credentials
                  consumer
                  +twitter_access_token+
                  +twitter_access_token_secret+
                  :get
                  "https://api.twitter.com/1.1/statuses/user_timeline.json"
                  {:screen_name "fommil"}))

(defn twitter-request
  "Wraps `http/request' with OAuth1.0 authentication assuming that the consumer and access tokens are defined."
  [method url query-params]
  (let [consumer (oauth/make-consumer
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
    
    )
  )


(defn -main []
  (println "Hello, World!"
           (http/request
            {:method :get
             :url "https://api.twitter.com/1.1/statuses/user_timeline.json"
             :query-params (merge credentials {:screen_name "fommil"})
             })
           ))



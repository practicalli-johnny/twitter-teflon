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

(http/get "https://api.twitter.com/1.1/statuses/user_timeline.json"
          {:query-params credentials})


(defn -main []
  (println "Hello, World!"
           ))



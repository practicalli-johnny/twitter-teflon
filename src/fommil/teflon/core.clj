(ns fommil.teflon.core
  (:require
   [oauth.client :as oauth]
   ;;[oauth.twitter :as twitter]
   [clj-http.client :as http]))

(load-file "config.clj")

;; (def request-token (twitter/oauth-request-token
;;                     +twitter_consumer_key+
;;                     +twitter_consumer_secret+))

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
                  :GET
                  "https://api.twitter.com/1.1/statuses/user_timeline.json"
                  {:screen_name "fommil"}))


(oauth/access-token consumer (oauth/request-token consumer))

(http/request
 {:method :get
  :url "https://api.twitter.com/1.1/statuses/user_timeline.json"
  :query-params (merge credentials {:screen_name "fommil"})
  })


(defn -main []
  (println "Hello, World!"
           ))



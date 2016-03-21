(ns fommil.teflon
  (:use
   [clojure.core.async :as async :only [<! >! <!! go go-loop]])
  (:require
   [fommil.channels :as channels]
   ;; would be nice to `:as twitter' the functions and `:refer' the dynamic vars
   [fommil.twitter :refer :all]))

(defn delete-tweet?
  "True if this tweet is not good enough to keep."
  [tweet]
  (let [{retweets :retweet_count
         likes :favorite_count} tweet]
    (and (< retweets 5) (< likes 5))))

;; would be nice to load the config more idiomatically, this feels hacky...
(load-file "config.clj")

(defn -main []
  (println "hello")

  (binding
    [*twitter_consumer_key*        +twitter_consumer_key+
     *twitter_consumer_secret*     +twitter_consumer_secret+
     *twitter_access_token*        +twitter_access_token+
     *twitter_access_token_secret* +twitter_access_token_secret+]
    (let [tweets (channels/rate-limited
                  3000
                  (user_timeline_all +twitter_username+))]
      (loop []
        (when-let [batch (<!! tweets)]
          (println "GOT" (count batch) "tweets ["
                   (first (map :id batch)) "," (last (map :id batch))
                   "]")
          (recur)))))

  (println "goodbye"))

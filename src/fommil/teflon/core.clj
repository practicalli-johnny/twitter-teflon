(ns fommil.teflon.core
  (:use
   [clojure.core.async :as async :only [<! >! <!! go go-loop]])
  (:require
   [clojure.edn :as edn]
   [fommil.channels :as channels]
   ;; would be nice to `:as twitter' the functions and `:refer' the dynamic vars
   [fommil.twitter :refer :all]))

(defn delete-tweet?
  "True if this tweet is not good enough to keep."
  [tweet]
  (let [{retweets :retweet_count
         likes :favorite_count} tweet]
    (and (< retweets 5) (< likes 5))))

(defn -main []
  ;; destructuring doesn't work in `binding' forms
  (let [{key          :twitter_consumer_key
         secret       :twitter_consumer_secret
         token        :twitter_access_token
         token_secret :twitter_access_token_secret
         user         :twitter_username}
        (edn/read-string (slurp "config.edn"))]
    (binding
     [*twitter_consumer_key*        key
      *twitter_consumer_secret*     secret
      *twitter_access_token*        token
      *twitter_access_token_secret* token_secret]
      (let [tweets (channels/rate-limited
                    3000
                    (user_timeline_all user))]
        (loop []
          (when-let [batch (<!! tweets)]
            (println "GOT" (count batch) "tweets ["
                     (first (map :id batch)) "," (last (map :id batch))
                     "]")
            (recur)))))))

;; Copyright (C) 2016 Sam Halliday
;; Licence: http://www.gnu.org/licenses/lgpl.html

;; Wrapper around http-kit and oath specifically for the Twitter API
(ns fommil.twitter
  (:require
   [clojure.string :as string]
   [fommil.oauth :as oauth]
   [org.httpkit.client :as http]
   [cheshire.core :as json])
  (:use
   [clojure.core.async :as async :only [<! >! go go-loop]])
  (:import
   [fommil.oauth Consumer]))

(def ^:dynamic *twitter_consumer_key*)
(def ^:dynamic *twitter_consumer_secret*)
(def ^:dynamic *twitter_access_token*)
(def ^:dynamic *twitter_access_token_secret*)

(defn request
  "Wraps `http/request' with OAuth1.0 authentication assuming that the
  consumer and access tokens are defined. Returns a channel."
  [method endpoint query-params]
  (let [ch (async/chan)
        url (string/join "" ["https://api.twitter.com/" endpoint])
        consumer (oauth/Consumer.
                  *twitter_consumer_key*
                  *twitter_consumer_secret*
                  "https://api.twitter.com/oauth/request_token"
                  "https://api.twitter.com/oauth/access_token"
                  "https://api.twitter.com/oauth/authorize"
                  :hmac-sha1)
        credentials (oauth/credentials
                     consumer
                     *twitter_access_token*
                     *twitter_access_token_secret*
                     method
                     url
                     query-params)
        payload (merge credentials query-params)]
    (http/request
     {:method method :url url :query-params payload}
     (fn [response]
       (if (= 200 (:status response))
         (async/put! ch response)
         (do (async/close! ch)
             (throw (Exception. response))))))
    ch))

(defn user_timeline
  "Return a channel containing a batch of tweets (id strictly less than
  the max_id) for the given user."
  [user max_id]
  (let [in (->>
            (if max_id {:max_id (dec max_id)} {})
            ;; 200 is the maximum allowed
            (merge {:screen_name user :count 200})
            (request
             :get "1.1/statuses/user_timeline.json"))
        out (async/chan)]
    (go
      (if-let [resp (<! in)]
        (let [json (:body resp)
              parsed (json/parse-string json true)]
          (if (empty? parsed)
            (async/close! out)
            (>! out parsed)))
        (async/close! out)))
    out))

(defn user_timeline_all
  "Return a channel containing tweets for the `user', newest first."
  [user]
  (let [out (async/chan)]
    (go-loop [previous nil]
      (if-let [tweets (<! (user_timeline user previous))]
        (do
          (>! out tweets)
          (recur (:id (last tweets))))
        (async/close! out)))
    out))

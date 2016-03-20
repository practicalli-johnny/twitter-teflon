(ns fommil.oauth-test
  (:require [fommil.oauth :as oauth] :reload-all)
  (:import [fommil.oauth Consumer])
  (:use clojure.test))

(deftest
  credentials
  (let [consumer (oauth/Consumer.
                  "dpf43f3p2l4k3l03"
                  "kd94hf93k423kf44"
                  "https://api.twitter.com/oauth/request_token"
                  "https://api.twitter.com/oauth/access_token"
                  "https://api.twitter.com/oauth/authorize"
                  :hmac-sha1)
        credentials (oauth/credentials
                     consumer
                     "nnch734d00sl2jdk"
                     "pfkkdhi9sl3r4s00"
                     :get
                     "https://api.twitter.com/1.1/statuses/user_timeline.json"
                     0
                     {:screen_name "fommil"})]
    ;; some fields subject to randomness
    (is (= (dissoc credentials :oauth_nonce :oauth_signature)
           {:oauth_consumer_key "dpf43f3p2l4k3l03"
            :oauth_signature_method "HMAC-SHA1"
            :oauth_timestamp 0
            :oauth_version "1.0"
            :oauth_token "nnch734d00sl2jdk"}))))

;; Copyright (C) 2009 - 2016 Matt Revelle
;; Licence: https://opensource.org/licenses/BSD-2-Clause

;; Copied from https://github.com/mattrepl/clj-oauth
;;
;; This minimial version of clj-oauth removes blocking HTTP calls
;; and unnecessary transient dependencies.
(ns fommil.oauth
  (:require
   [fommil.signature :as sig]
   [clojure.string :as string]))

(defrecord
 #^{:doc "OAuth consumer"}
   Consumer [key secret request-uri access-uri authorize-uri signature-method])

(defn credentials
  "Return authorization credentials needed for access to protected resources.
  The key-value pairs returned as a map will need to be added to the
  Authorization HTTP header or added as query parameters to the
  request."
  ([consumer token token-secret request-method request-uri request-params]
   (let [time (sig/msecs->secs (System/currentTimeMillis))]
     (credentials consumer token token-secret request-method request-uri time request-params)))
  ([consumer token token-secret request-method request-uri time request-params]
   (let [unsigned-oauth-params (sig/oauth-params
                                consumer
                                (sig/rand-str 30)
                                time
                                token)
         unsigned-params       (merge request-params unsigned-oauth-params)
         signature             (sig/sign
                                consumer
                                (sig/base-string
                                 (-> request-method
                                     sig/as-str
                                     string/upper-case)
                                 request-uri
                                 unsigned-params)
                                token-secret)]
     (assoc unsigned-oauth-params :oauth_signature signature))))

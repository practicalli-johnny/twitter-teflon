;; Copyright (C) 2009 - 2016 Matt Revelle
;; Licence: https://opensource.org/licenses/BSD-2-Clause

;; Copied from https://github.com/mattrepl/clj-oauth

(ns fommil.digest
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec)))

(defn hmac
  "Calculate HMAC signature for given data."
  [^String key ^String data]
  (let [hmac-sha1 "HmacSHA1"
        signing-key (SecretKeySpec. (.getBytes key) hmac-sha1)
        mac (doto (Mac/getInstance hmac-sha1) (.init signing-key))]
    (String. (org.apache.commons.codec.binary.Base64/encodeBase64
              (.doFinal mac (.getBytes data)))
             "UTF-8")))

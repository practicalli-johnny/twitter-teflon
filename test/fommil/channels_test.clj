;; Copyright (C) 2016 Sam Halliday
;; Licence: http://www.gnu.org/licenses/lgpl.html

;; potentially flakey tests because they rely on timing and if a GC
;; happens, or we get a slow machine, things might fail.
(ns fommil.channels-test
  (:use
   [clojure.core.async :as async :only [<!! >! go go-loop]])
  (:require
   [midje.sweet :refer :all]
   [fommil.channels :refer :all]))

(defmacro timed
  "Returns a map containing `:result' of `expr' with its `:time' (millis)."
  [expr]
  `(let [start# (System/currentTimeMillis)
         ret# ~expr
         time# (- (System/currentTimeMillis) start#)]
     {:result ret# :time time#}))

(fact "heartbeats can have an initial delay"
      (timed (<!! (heartbeat 50)))
      => (just {:result :ping :time #(<= 0 % 20)})
      (timed (<!! (heartbeat 50 0)))
      => (just {:result :ping :time #(<= 0 % 20)})
      (timed (<!! (heartbeat 50 10)))
      => (just {:result :ping :time #(<= 5 % 20)})
      (timed (<!! (heartbeat 50 50)))
      => (just {:result :ping :time #(<= 50 % 60)}))

(fact "heartbeats arrive on time (on average)"
      (timed (<!! (async/partition 5 (heartbeat 50))))
      => (just {:result [:ping :ping :ping :ping :ping]
                :time #(<= 190 % 210)}))

(fact "channels can be rate limited"
      (let [counter (atom 0)
            in      (async/chan)
            limited (rate-limited 10 in)
            start   (System/currentTimeMillis)]
        (go-loop []
          (dosync (swap! counter inc))
          (>! in :foo)
          (recur))
        (while (< (- (System/currentTimeMillis) start) 100)
          (<!! limited))
        @counter)
      => #(<= 5 % 15))

(fact "rate-limited channels are closed correctly"
      (let [in (async/chan)
            limited (rate-limited 10 in)]
        (go []
            (>! in :foo)
            (async/close! in)
            (>! in :foo))
        (<!! limited)
        (<!! limited))
      => nil)

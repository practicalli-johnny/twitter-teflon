;; Copyright (C) 2016 Sam Halliday
;; Licence: http://www.gnu.org/licenses/lgpl.html

(ns fommil.channels-test
  (:use
   [clojure.core.async :as async :only [<!!]])
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
      => (just {:result :ping :time #(<= 0 % 10)})
      (timed (<!! (heartbeat 50 0)))
      => (just {:result :ping :time #(<= 0 % 10)})
      (timed (<!! (heartbeat 50 10)))
      => (just {:result :ping :time #(<= 5 % 20)})
      (timed (<!! (heartbeat 50 50)))
      => (just {:result :ping :time #(<= 50 % 60)})
 )

(fact "heartbeats arrive on time (on average)"
      (timed (<!! (async/partition 5 (heartbeat 50))))
      => (just {:result [:ping :ping :ping :ping :ping]
                :time #(<= 190 % 210)})
      )

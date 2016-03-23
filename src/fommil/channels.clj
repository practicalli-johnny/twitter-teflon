;; Copyright (C) 2016 Sam Halliday and Paul Butcher
;; Licence: http://www.gnu.org/licenses/lgpl.html

;; Utility functions for working with channels.
(ns fommil.channels
  (:use
   [clojure.core.async :as async :only [<! >! go go-loop]]))

(defn heartbeat
  "Return a channel that pings every `interval' millis after an initial `delay'."
  ([interval] (heartbeat interval 0))
  ([interval delay]
   (let [out (async/chan)]
     (go
       (<! (async/timeout delay))
       (>! out :ping)
       (loop []
         (<! (async/timeout interval))
         ;; has an initial delay
         (when (>! out :ping)
           (recur))))
     out)))

(defn rate-limited
  "Rate limit `in' to a maximum of one message every `internal' millis."
  [interval in]
  (let [out (async/chan)
        hb (heartbeat interval)]
    (go-loop []
      (<! hb)
      (if-let [read (<! in)]
        (when (>! out read)
          (recur))
        ;; do I really need to close my output if my input closes?
        (async/close! out)))
    out))

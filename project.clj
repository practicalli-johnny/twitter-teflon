(defproject twitter-teflon "0.1.0-SNAPSHOT"
  :description "Delete all your low-score tweets"
  :url "https://github.com/fommil/twitter-teflon"
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.5.0"]]
  :main fommil.teflon.core)

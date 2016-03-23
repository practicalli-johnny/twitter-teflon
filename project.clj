(defproject twitter-teflon "0.1.0-SNAPSHOT"
  :description "Delete all your low-score tweets"
  :url "https://github.com/fommil/twitter-teflon"
  :license {:name "GNU Lesser General Public License"
            :url "https://www.gnu.org/copyleft/lesser.html"}
  :plugins [[lein-cljfmt "0.4.1"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [http-kit "2.1.19"]
                 [commons-codec/commons-codec "1.8"]
                 [org.bouncycastle/bcprov-jdk15on "1.54"]
                 [org.bouncycastle/bcpkix-jdk15on "1.54"]
                 [cheshire "5.5.0"]]
  :main fommil.teflon.core)

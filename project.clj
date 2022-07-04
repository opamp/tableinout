(defproject tableinout "0.1.0-SNAPSHOT"
  :description "Table input/output library"
  :url "https://github.com/opamp/tableinout"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.apache.poi/poi "5.2.2"]
                 [org.apache.poi/poi-ooxml "5.2.2"]
                 [org.apache.logging.log4j/log4j-core "2.18.0"]]
  :repl-options {:init-ns tableinout.core})

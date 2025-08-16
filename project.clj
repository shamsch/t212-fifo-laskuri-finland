(defproject t212-fifo-laskuri-finland "0.1.0-SNAPSHOT"
  :description "A tax calculator using FIFO method for Finnish capital gains tax on Trading 212 investment data"
  :url "https://github.com/shamsch/t212-fifo-laskuri-finland"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.csv "1.0.1"]]
  :main ^:skip-aot t212-fifo-laskuri-finland.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})

(defproject webttt "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main server.starter
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [maniginam/server "1.0-SNAPSHOT"]
                 [maniginam/tic-tac-toe "1.0.0-SNAPSHOT"]
                 [quil "3.1.0"]
                 [clj-http "3.10.1"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]}}
  :plugins [[speclj "3.3.2"]]
  :test-paths ["spec"]
  )



(defproject webttt "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Maniginam Web TicTacToe"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main server.starter
  :dependencies [[clj-http "3.10.1"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 [maniginam/server "1.0-SNAPSHOT"]
                 [maniginam/tic-tac-toe "1.0.0-SNAPSHOT"]
                 [org.clojure/clojure "1.10.1"]
                 [clj-time "0.15.2"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]}}
  :plugins [[speclj "3.3.2"]]
  :test-paths ["spec"]
  )



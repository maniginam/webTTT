(ns spec-helper
	(:require [clojure.java.io :as io]
						[speclj.core :refer :all]
						[server.starter :as starter]))

(def default-game {:console        :web
									 :status         :waiting
									 :persistence    {:db :mysql :dbname "ttt"}
									 :users          0
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type :computer}
									 :player2        {:player-num 2 :piece "O" :type :computer}})

(def empty-board [0 1 2 3 4 5 6 7 8])

(def request-map {"Host" "localhost:3141" "httpVersion" "HTTP/1.1" "method" "GET" "resource" "/ttt"})

(defn stop []
	(assert @starter/server-atom "no server running")
	(.end (:host @starter/server-atom))
	(.close @starter/socket-atom)
	(reset! starter/server-atom nil)
	(reset! starter/socket-atom nil)
	(Thread/sleep 100))
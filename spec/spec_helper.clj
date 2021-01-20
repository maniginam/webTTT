(ns spec-helper
	(:require [clojure.java.io :as io]
						[speclj.core :refer :all]
						[server.starter :as starter]
						[master.core :as tcore]))

(def default-game {:console        :mock
									 :status         :waiting
									 :persistence    {:db :mock :dbname "ttt"}
									 :users          0
									 :board-size     3
									 :level          :easy
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type :mock}
									 :player2        {:player-num 2 :piece "O" :type :mock}})

(def default-cookie {:cookieID    -3141
										 :persistence {:db :mock :dbname "ttt"}
										 :status      :waiting
										 :users       0
										 :board-size  3
										 ;:board       [0 1 2 3 4 5 6 7 8]
										 :level       :easy
										 :player1     {:player-num 1 :piece "X" :type :mock}
										 :player2     {:player-num 2 :piece "O" :type :mock}})

(def empty-board [0 1 2 3 4 5 6 7 8])
(def mock-move (atom 0))
(def games (atom {}))

(defmethod tcore/select-box :mock [_ game] @mock-move)

(defmethod tcore/update-game-with-id :mock [game]
	(let [id (if (nil? (:gameID game)) (rand-int 999) (:gameID game))
				game (assoc game :gameID id)]
		(swap! games assoc id game)
		game))

(defmethod tcore/save-game :mock [game]
	(let [id (if (nil? (:gameID game)) (rand-int 999) (:gameID game))
				game (assoc game :gameID id)]
		(swap! games (fn [game-map] (assoc game-map id game)))
		game))

(defmethod tcore/save-turn :mock [game]
	(tcore/save-game game))

(defmethod tcore/load-game :mock [game]
	(get @games (:cookieID game)))

(defmethod tcore/draw-state :mock [game]
	(tcore/draw-state (assoc game :console :web)))

(def request-map {"Host" "localhost:3141" "httpVersion" "HTTP/1.1" "method" "GET" "resource" "/ttt"})

(defn stop []
	(assert @starter/server-atom "no server running")
	(.end (:host @starter/server-atom))
	;(.close @starter/socket-atom)
	(reset! starter/server-atom nil)
	;(reset! starter/socket-atom nil)
	(Thread/sleep 100))
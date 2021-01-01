(ns game.game-manager
	(:require [master.core :as tcore]
						[ttt.board :as board]
		;[gui.gui]
						[ttt.terminal]
						[master.game-master]))

(def game (atom {:status         :waiting
								 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}
								 :users          nil
								 :board-size     3
								 :current-player :player1
								 :player1        {:player-num 1 :piece "X" :type nil}
								 :player2        {:player-num 2 :piece "O" :type nil}}))

(defmethod tcore/set-parameters :waiting [waiting-game]
	(let [status (cond (zero? (:users @game)) :level-setup
										 (= 1 (:users @game)) :player-setup
										 (= 2 (:users @game)) :board-setup)]
		(swap! game assoc :status status)))

(defmethod tcore/set-parameters :player-setup [not-setup-game]
	(swap! game assoc :status :level-setup))

(defmethod tcore/set-parameters :level-setup [level-setup-game]
	(swap! game assoc :status :board-setup))

(defmethod tcore/set-parameters :board-setup [game-without-board]
	(let [board-size (:board-size game-without-board)
				board (board/create-board board-size)]
		(swap! game assoc :board board :status :playing)))

(defn manage-game [entries]
		(doseq [entry entries]
			(let [key (key entry)
						val (try (Integer/parseInt (val entry))
										 (catch Exception e
											 (if (= :player key)
												 (val entry)
												 (keyword (val entry)))))]
				(cond (= val "X") (do (swap! game assoc :player1 (assoc (:player1 @game) :type :human))
															(swap! game assoc :player2 (assoc (:player2 @game) :type :computer)))
							(= val "O") (do (swap! game assoc :player1 (assoc (:player1 @game) :type :computer))
															(swap! game assoc :player2 (assoc (:player2 @game) :type :human)))
							:else (swap! game assoc key val)))
			(tcore/set-parameters @game)
			;(if (= :ready-to-play (:status @game))
			;	(tcore/run-game @game))
			))



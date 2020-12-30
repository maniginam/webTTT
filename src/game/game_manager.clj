(ns game.game-manager
	(:require [master.core :as tcore]
		[ttt.board :as board]))

(def game (atom {:status         :waiting
								 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}
								 :users          nil
								 :board-size     3
								 :current-player :player1
								 :player1        {:player-num 1 :piece "X" :type nil}
								 :player2        {:player-num 2 :piece "O" :type nil}}))

(defmethod tcore/set-parameters :waiting [game]
	(let [status (cond (zero? (:users game)) :level-setup
										 (= 1 (:users game)) :player-setup
										 (= 2 (:users game)) :board-setup)]
		status))

(defmethod tcore/set-parameters :player-setup [game]
	(let [status :level-setup]
		status))

(defmethod tcore/set-parameters :level-setup [game]
	(let [status :board-setup]
		status))

(defmethod tcore/set-parameters :board-setup [game]
	(let [board-size (:board-size game)
				board (board/create-board board-size)
				status :ready-to-play]
		(swap! game assoc :board board)
		status))

(defn manage-game [entries]
	(doseq [entry entries]
		(let [key (key entry)
					val (try (Integer/parseInt (val entry))
									 (catch Exception e
										 (val entry)))]
			(cond (= val (get :piece (:player-1 game))) (swap! game assoc (:piece (:player1 @game)) val)
						(= val (get :piece (:player-2 game))) (swap! game assoc (:piece (:player1 @game)) val)
						:else (swap! game assoc key val)))
	(let [status (tcore/set-parameters @game)]
		(swap! game assoc :status status))))



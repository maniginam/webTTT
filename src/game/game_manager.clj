(ns game.game-manager
	(:require [master.game-master :as game]
						[ttt.board :as board]
						[master.core :as tcore]
						[html.game-writer :as writer]
						[clojure.string :as str])
	(:import (httpServer Server)))

(def default-game {:console        :web
									 :status         :waiting
									 :users          nil
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type :computer}
									 :player2        {:player-num 2 :piece "O" :type :computer}
									 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}})
(def game (atom default-game))

(defmethod tcore/set-parameters :waiting [not-setup-game]
	(swap! game assoc :status :user-setup))

(defn set-players [users]
	(cond (zero? users) (swap! game assoc :status :level-setup :player1 {:type :computer :piece "X" :player-num 1} :player2 {:type :computer :piece "O" :player-num 2})
				(= 2 users) (swap! game assoc :status :board-setup :player1 {:type :human :piece "X" :player-num 1} :player2 {:type :human :piece "O" :player-num 2})
				:else (swap! game assoc :status :player-setup)))

(defmethod tcore/set-parameters :user-setup [game-entry]
	(let [users (Integer/parseInt (:users (get game-entry :entry)))]
		(swap! game assoc :users users)
		(set-players users)))

(defmethod tcore/set-parameters :player-setup [game-entry]
	(let [human (:piece (get game-entry :entry))]
		(cond (= "X" human) (do (swap! game assoc :player1 (assoc (:player1 @game) :type :human))
														(swap! game assoc :player2 (assoc (:player2 @game) :type :computer)))
					(= "O" human) (do (swap! game assoc :player1 (assoc (:player1 @game) :type :computer))
														(swap! game assoc :player2 (assoc (:player2 @game) :type :human)))
					:else nil)
		(swap! game assoc :status :level-setup)))

(defmethod tcore/set-parameters :level-setup [game-entry]
	(let [level (:level (get game-entry :entry))]
		(swap! game assoc :level (keyword level) :status :board-setup)))

(defmethod tcore/set-parameters :board-setup [game-entry]
	(let [board-size (Integer/parseInt (:board-size (get game-entry :entry)))
				board (board/create-board board-size)]
		(swap! game assoc :board-size board-size :board board :status :ready-to-play)))

(defn setup-game [entry]
	(tcore/set-parameters (assoc @game :entry entry))
	(when (= :ready-to-play (:status @game))
		(reset! game (game/update-state @game))
		(tcore/draw-state @game)))

(defn reset-game []
	(reset! game default-game)
	(swap! game assoc :status :user-setup))

(defn manage-game [entry]
	(if (nil? entry)
		(reset-game)
		(setup-game entry)))






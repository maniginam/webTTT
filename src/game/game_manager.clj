(ns game.game-manager
	(:require [clojure.string :as str]
						[html.core :as hcore]
						[html.game-writer :as writer]
						[master.core :as tcore]
						[master.game-master :as game]
						[ttt.board :as board]))

(def default-game {:console        :web
									 :persistence    {:db :mysql :dbname "ttt"}
									 :status         :waiting
									 :users          nil
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type nil}
									 :player2        {:player-num 2 :piece "O" :type nil}})
(def game (atom default-game))

(defn restart! [game-to-restart]
	(let [last-game (:last-game @game)]
		(reset! game last-game)
		(swap! game assoc :console :web)
		(tcore/draw-state @game)
	@game))

(defmethod tcore/set-parameters :waiting [game-entry]
	(reset! game (game/update-state @game))
	(if (or (nil? (:last-game @game)) (game/game-over? (:last-game @game)))
		(swap! game assoc :status :user-setup)
		(swap! game assoc :status :restart?)))

(defmethod tcore/set-parameters :restart? [game-entry]
	(let [play-last-game? (:continue (get game-entry :entry))]
		(if (= "no" play-last-game?)
			(swap! game assoc :status :user-setup)
			(restart! @game))))

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

(defn play-turn [entry]
	(if (not (game/ai-turn? @game))
		(let [box (Integer/parseInt (:box entry))]
			(reset! game (game/update-game-with-move! @game box))
			(reset! game (game/update-state @game))
			(if (= :game-over (:status @game)) (hcore/write! @game)))
		(while (and (not (game/game-over? @game)) (game/ai-turn? @game))
			(reset! game (game/update-state @game)))))

(defn setup-game [entry]
	(tcore/set-parameters (assoc @game :entry entry))
	(swap! game assoc :console :web)
	(when (= :ready-to-play (:status @game))
		(reset! game (game/update-state @game))
		(tcore/draw-state @game)))

(defn reset-game []
	(reset! game default-game)
	(swap! game assoc :status :user-setup :console :web))

(defn manage-game [request]
	(let [entry (:entry request)]
		(cond (nil? entry) (reset-game)
					(= :setup (:responder request)) (setup-game entry)
					(= :playing (:responder request)) (play-turn entry)
					(= :play-again (:responder request)) (do (reset! game default-game) (swap! game assoc :status :user-setup))
					:else nil)))






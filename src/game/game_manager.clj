(ns game.game-manager
	(:require [html.core :as hcore]
						[html.game-writer]                                    ;multimethod
						[html.game-over-writer]                               ;multimethod
						[html.draw]                                           ;multimethod
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
(def games (atom {}))
(def gameNum (atom 0))

(defmethod tcore/set-parameters :waiting [game-entry]
	(let [game (game/update-state default-game)
				status (if (or (nil? (:last-game game)) (game/game-over? (:last-game game)))
								 :user-setup
								 :restart?)
				new-game (assoc game :status status :gameID @gameNum)]
		(swap! gameNum inc)
		(swap! games assoc (:gameID new-game) new-game)
		new-game))

(defn restart! [game]
	(let [last-game (assoc (:last-game game) :console :web)]
		(tcore/draw-state last-game)
		last-game))

(defmethod tcore/set-parameters :restart? [game]
	(let [play-last-game? (:continue (get game :entry))]
		(if (= play-last-game? "no")
			(assoc game :status :user-setup :console :web)
			(restart! game))))

(defn set-players [game]
	(let [users (:users game)]
		(cond (zero? users) (assoc game :status :level-setup :player1 {:type :computer :piece "X" :player-num 1} :player2 {:type :computer :piece "O" :player-num 2})
					(= 2 users) (assoc game :status :board-setup :player1 {:type :human :piece "X" :player-num 1} :player2 {:type :human :piece "O" :player-num 2})
					:else (assoc game :status :player-setup))))

(defmethod tcore/set-parameters :user-setup [game]
	(let [users (Integer/parseInt (:users (get game :entry)))]
		(set-players (assoc game :users users))))

(defmethod tcore/set-parameters :player-setup [game]
	(let [human (:piece (get game :entry))]
		(cond (= "X" human) (do (assoc game :player1 (assoc (:player1 @game) :type :human))
														(assoc game :player2 (assoc (:player2 @game) :type :computer)))
					(= "O" human) (do (assoc game :player1 (assoc (:player1 @game) :type :computer))
														(assoc game :player2 (assoc (:player2 @game) :type :human)))
					:else nil)
		(assoc game :status :level-setup)))

(defmethod tcore/set-parameters :level-setup [game]
	(let [level (:level (get game :entry))]
		(assoc game :level (keyword level) :status :board-setup)))

(defmethod tcore/set-parameters :board-setup [game]
	(let [board-size (Integer/parseInt (:board-size (get game :entry)))
				board (board/create-board board-size)]
		(assoc game :board-size board-size :board board :status :ready-to-play)))

(defn play-turn [game entry]
	(println "PLAY-TURN game: " game)
	(println "entry: " entry)
	(if (not (game/ai-turn? game))
		(let [box (Integer/parseInt (:box entry))]
			(if (string? (nth (:board game) box))
				game
				(let [game-with-next-round (game/update-state (game/update-game-with-move! game box))]
					(if (and (not (game/game-over? game)) (game/ai-turn? game))
						(game/update-state game-with-next-round)
						game-with-next-round)
					(loop [game game]
						(if (or (game/game-over? game) (not (game/ai-turn? game)))
							game
							(recur (game/update-state game)))))))))

(defn get-state-of-game [cookie]
	(if (nil? cookie)
		(let [game (assoc default-game :gameID (swap! gameNum inc))]
			(swap! games assoc (:gameID game) game)
			game)
		(let [state (get cookie :state-id)
					gameID (get cookie :gameID)]
			(cond (nil? gameID) default-game
						(= "oreo" state) (get @games gameID)
						(= "snickerdoodle" state) (tcore/load-game (assoc default-game :gameID gameID :console :web))))))

(defn setup-game [game entry]
	(let [game-in-new-phase (tcore/set-parameters (assoc game :entry entry))]
		(if (= :ready-to-play (:status game-in-new-phase))
			(game/update-state game-in-new-phase)
			game-in-new-phase)))

(defn manage-game [request]
	(let [entry (:entry request)
				current-state-game (get-state-of-game (:cookie request))
				game (cond (= :setup (:responder request)) (setup-game current-state-game entry)
									 (= :playing (:responder request)) (play-turn current-state-game entry)
									 (= :play-again (:responder request)) (assoc default-game :status :user-setup)
									 :else default-game)]
		(println "MANAGER-GAME game: " game)
		game))






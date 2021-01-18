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
									 :player1        {:player-num 1 :piece "X" :type :computer}
									 :player2        {:player-num 2 :piece "O" :type :computer}})

(def games (atom {}))
(def gameID (atom 0))

(defn save-status [game]
	(let [updated-game (assoc game :status (:status game)
																 :users (:users game)
																 :player1 (:player1 game)
																 :player2 (:player2 game)
																 :level (:level game)
																 :board-size (:board-size game))]
		(swap! games assoc (:gameID game) updated-game)
		(get @games (:gameID game))))

(defmethod tcore/set-parameters :waiting [game]
	(let [game (game/update-state game)
				status (if (or (nil? (:last-game game)) (game/game-over? (:last-game game)))
								 :user-setup
								 :restart?)
				new-game (assoc game :status status)]
		(save-status new-game)))

(defn continue-last-game! [game]
	;; TODO - GLM : MAYBE UPDATE GAME
	(tcore/draw-state game)
	game)

(defmethod tcore/set-parameters :restart? [game]
	(let [play-last-game? (:continue (get game :entry))]
		(if (= play-last-game? "no")
			(save-status (assoc game :status :user-setup))
			(continue-last-game! (assoc (:last-game game) :console :web)))))

(defn new-status-based-on-users [users]
	(cond (zero? users) :level-setup
				(= 1 users) :player-setup
				(= 2 users) :board-setup))

(defmethod tcore/set-parameters :user-setup [game]
	(let [users (Integer/parseInt (:users (get game :entry)))
				status (new-status-based-on-users users)]
		(assoc game :status status :users users)))

(defmethod tcore/set-parameters :player-setup [game]
	(let [human (:piece (get game :entry))
				player1 (if (= "X" human) {:playerNum 1 :piece "X" :type :human} {:playerNum 1 :piece "X" :type :computer})
				player2 (if (= "O" human) {:playerNum 2 :piece "O" :type :human} {:playerNum 2 :piece "O" :type :computer})
				updated-game (assoc game :status :level-setup :player1 player1 :player2 player2)]
		(save-status updated-game)))

(defmethod tcore/set-parameters :level-setup [game]
	(let [level (:level (get game :entry))]
		(save-status (assoc game :level (keyword level) :status :board-setup))))

(defmethod tcore/set-parameters :board-setup [game]
	(let [board-size (Integer/parseInt (:board-size (get game :entry)))
				board (board/create-board board-size)]
		(save-status (assoc game :board-size board-size :board board :status :ready-to-play))))

(defn play-turn [game entry]
	(if (game/ai-turn? game)
		(game/update-state game)
		(let [box (Integer/parseInt (:box entry))]
			(if (string? (nth (:board game) box))
				game
				(let [game-with-next-round (game/update-state (game/update-game-with-move! game box))]
					(if (and (not (game/game-over? game-with-next-round)) (game/ai-turn? game-with-next-round))
						(game/update-state game-with-next-round)
						game-with-next-round))))))

(defn maybe-start! [game]
	(if (= :ready-to-play (:status game))
		(let [ready-game (assoc (merge default-game game) :status :playing)]
			(game/update-state ready-game))
		game))

(defn setup-game [game entry]
	(let [game-in-new-phase (tcore/set-parameters (assoc game :entry entry))]
		(maybe-start! game-in-new-phase)))

(defn get-state-of-game [request]
	(cond (nil? (:Cookie request)) (assoc default-game :gameID (swap! gameID inc))
				(= :playing (get (:Cookie request) :status)) (tcore/load-game (merge default-game (:Cookie request)))
				:else (:Cookie request)))

(defn manage-game [request]
	(let [entry (:entry request)
				current-state-game (get-state-of-game request)]
		(cond (= :setup (:responder request)) (setup-game current-state-game entry)
					(= :playing (:responder request)) (play-turn current-state-game entry)
					(= :play-again (:responder request)) (assoc default-game :status :user-setup)
					:else default-game)))






(ns game.game-manager
	(:require [html.core :as hcore]
						[html.game-writer]                                    ;multimethod
						[html.game-over-writer]                               ;multimethod
						[html.draw]                                           ;multimethod
						[master.core :as tcore]
						[master.game-master :as game]
						[ttt.board :as board]
						[clj-time.core :as time]))

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

(defn bake-cookieID []
	(let [now (time/now)]
		(float (apply + [(* (time/hour now) 60) (time/minute now) (/ (time/second now) 60)]))))

(defn save-status [game]
	(let [updated-game (assoc game :status (:status game)
																 :users (:users game)
																 :player1 (:player1 game)
																 :player2 (:player2 game)
																 :level (:level game)
																 :board-size (:board-size game))]
		(swap! games assoc (:cookieID game) updated-game)
		(get @games (:cookieID game))))

(defmethod tcore/set-parameters :waiting [game]
	(let [default-game-with-last-game (game/update-state default-game)
				last-game (:last-game default-game-with-last-game)
				;(catch Exception e
				;	(assoc default-default-game-with-last-game :status :user-setup :cookieID (bake-cookieID)))
				status (if (or (nil? last-game) (game/game-over? last-game))
								 :user-setup
								 :restart?)
				new-game (assoc game :status status :last-game last-game)]
		(save-status new-game)))

(defn continue-last-game! [game]
	;; TODO - GLM : MAYBE UPDATE GAME
	(let [continued-game (assoc (merge default-game (:last-game game)) :status :playing)]
		(tcore/draw-state continued-game)
	(assoc continued-game :cookieID (+ (:cookieID game) 1))))

(defmethod tcore/set-parameters :restart? [game]
	(let [play-last-game? (get game :entry)]
		(if (= play-last-game? "no")
			(save-status (assoc game :status :user-setup))
			(continue-last-game! game))))

(defn new-status-based-on-users [game users]
	(cond (zero? users) (assoc game :status :level-setup :users 0 :player1 {:playerNum 1 :piece "X" :type :computer} :player2 {:playerNum 2 :piece "O" :type :computer})
				(= 1 users) (assoc game :status :player-setup :users 1)
				(= 2 users) (assoc game :status :board-setup :users 2 :player1 {:playerNum 1 :piece "X" :type :human} :player2 {:playerNum 2 :piece "O" :type :human})))

(defmethod tcore/set-parameters :user-setup [game]
	(if (= "setup" (:entry game))
		game
		(let [users (Integer/parseInt (get game :entry))
					updated-game (new-status-based-on-users game users)]
			(save-status updated-game))))

(defmethod tcore/set-parameters :player-setup [game]
	(let [human (get game :entry)
				player1 (if (= "X" human) {:playerNum 1 :piece "X" :type :human} {:playerNum 1 :piece "X" :type :computer})
				player2 (if (= "O" human) {:playerNum 2 :piece "O" :type :human} {:playerNum 2 :piece "O" :type :computer})
				updated-game (assoc game :status :level-setup :player1 player1 :player2 player2)]
		(save-status updated-game)))

(defmethod tcore/set-parameters :level-setup [game]
	(let [level (get game :entry)]
		(save-status (assoc game :level (keyword level) :status :board-setup))))

(defmethod tcore/set-parameters :board-setup [game]
	(let [board-size (Integer/parseInt (get game :entry))
				board (board/create-board board-size)]
		(save-status (assoc game :board-size board-size :board board :status :ready-to-play))))

(defn maybe-game-over! [game]
	(if (= :game-over (:status game))
		(tcore/draw-state game))
	game)

(defn play-turn [game entry]
	(let [game (if (game/ai-turn? game)
							 (game/update-state (merge default-game game))
							 (let [box (Integer/parseInt entry)]
								 (if (string? (nth (:board game) box))
									 game
									 (game/update-state (game/update-game-with-move! (merge default-game game) box)))))]
		(maybe-game-over! game)))
;(if (and (not (game/game-over? game-with-next-round)) (game/ai-turn? game-with-next-round))
;	(game/update-state game-with-next-round)
;	game-with-next-round))))))

(defn maybe-start! [game]
	(if (= :ready-to-play (:status game))
		(let [ready-game (merge default-game game)
					updated-game (game/update-state ready-game)]
			(tcore/draw-state updated-game)
			(assoc updated-game :cookieID (bake-cookieID)))
		(assoc game :cookieID (bake-cookieID))))

(defn setup-game [game entry]
	(let [game-in-new-phase (tcore/set-parameters (assoc game :entry entry))]
		(maybe-start! game-in-new-phase)))

(defn load-game-by-id [request]
	(let [game (tcore/load-game (merge default-game (:Cookie request)))]
		(if (nil? (:board game)) (assoc game :board (board/create-board (:board-size game))) game)))


(defn get-state-of-game [request]
	(cond (nil? (get (:Cookie request) :cookieID)) (assoc default-game :cookieID (bake-cookieID))
				(= :playing (get (:Cookie request) :status)) (load-game-by-id request)
				:else (:Cookie request)))

(defn manage-game [request]
	(println "MANAGER request: " request)
	(let [entry (:entry request)
				current-state-game (if (= "setup" entry) (get-state-of-game default-game) (get-state-of-game request))]
		(println "current-state-game: " current-state-game)
		(cond (= :setup (:responder request)) (setup-game current-state-game entry)
					(= :playing (:responder request)) (play-turn current-state-game entry)
					(= :play-again (:responder request)) (assoc default-game :status :user-setup)
					:else default-game)))






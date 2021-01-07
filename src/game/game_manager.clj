(ns game.game-manager
	(:require [master.game-master :as game]
						[ttt.board :as board]
						[master.core :as tcore]
						[html.game-writer :as writer]
						[clojure.string :as str]))

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
	(cond (zero? users) (swap! game assoc :player1 {:type :computer :piece "X" :player-num 1} :player2 {:type :computer :piece "O" :player-num 2})
				(= 2 users) (swap! game assoc :player1 {:type :human :piece "X" :player-num 1} :player2 {:type :human :piece "O" :player-num 2})
				:else nil))

(defmethod tcore/set-parameters :user-setup [waiting-game]
	(let [status (cond (zero? (:users @game)) :level-setup
										 (= 1 (:users @game)) :player-setup
										 (= 2 (:users @game)) :board-setup)]
		(set-players (:users @game))
		(swap! game assoc :status status)))

(defmethod tcore/set-parameters :player-setup [not-setup-game]
	(swap! game assoc :status :level-setup))

(defmethod tcore/set-parameters :level-setup [level-setup-game]
	(swap! game assoc :status :board-setup))

(defmethod tcore/set-parameters :board-setup [game-without-board]
	(let [board-size (:board-size game-without-board)
				board (board/create-board board-size)]
		(swap! game assoc :board board :status :ready-to-play)))

(defn manage-game [request entries]
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
						:else (swap! game assoc key val))))
	(tcore/set-parameters @game)
	(if (= :ready-to-play (:status @game))
		(do (reset! game (game/update-state @game))
			(tcore/draw-state @game)
				;(while (and (not (game/game-over? @game)) (game/ai-turn? @game))
				;	(reset! game (game/update-state @game)))
				;(if (game/ai-turn? @game-atom)
				;	(tcore/run-game @game-atom))
				)
		)
	)





(ns game.human-turns
	(:require [game.game-manager :as manager]
						[spec-helper :as helper]
						[speclj.core :refer :all]))

(describe "Human"

	(context "plays"
		(it "round 1 of game"
			(reset! helper/mock-move 0)
			(reset! helper/games
							{-3141 (assoc helper/default-game
											 :status :playing :current-player :player1
											 :board [0 1 2 3 4 5 6 7 8]
											 :player1 {:type :human :piece "X" :player-num 1}
											 :player2 {:player-num 2 :piece "O" :type :mock})})
			(let [request-for-play {:entry {:box "4"} :responder :playing :Cookie (merge helper/default-game (assoc helper/default-cookie :status :playing :gameID -3141 :player1 {:player-num 1 :piece "X" :type :human} :player2 {:player-num 2 :piece "O" :type :mock}))}
						game (manager/manage-game request-for-play)]
				(should= :player1 (:current-player game))
				(should= "X" (nth (:board game) 4))
				(should= "O" (nth (:board game) 0))
				(should-not (:game-over game))))

		(it "player1 wins"
			(reset! helper/mock-move 3)
			(reset! helper/games
							{-3141 (assoc helper/default-game
											 :status :playing :current-player :player1
											 :board ["X" "X" 2 3 4 5 6 7 8]
											 :player1 {:type :human :piece "X" :player-num 1}
											 :player2 {:player-num 2 :piece "O" :type :mock})})
			(let [request-for-play {:entry  {:box "2"} :responder :playing
															:Cookie (merge helper/default-game (assoc helper/default-cookie :status :playing :gameID -3141 :player1 {:player-num 1 :piece "X" :type :human} :player2 {:player-num 2 :piece "O" :type :mock}))}
						game (manager/manage-game request-for-play)]
				(should= 1 (:winner game))
				(should= "X" (nth (:board game) 2))
				(should= :game-over (:status game))))

		)
	)
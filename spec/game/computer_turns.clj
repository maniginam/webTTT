(ns game.computer-turns
	(:require [game.game-manager :as manager]
						[spec-helper :as helper]
						[speclj.core :refer :all]))

(describe "Computer"

	(context "plays"
		(it "round 1 of game"
			(reset! helper/mock-move 0)
			(let [ready-game (assoc helper/default-cookie :status :ready-to-play)
						game (manager/maybe-start! ready-game)]
				(should= :playing (:status game))
				(should= :player2 (:current-player game))
				(should= 1 (count (filter #(= "X" %) (:board game))))
				(should-not (:game-over game))))

		(it "vs computer for cat's game"
			(reset! helper/mock-move 1)
			(reset! helper/games {-3141 (assoc helper/default-game :status :playing :board ["X" 1 "O" "O" "O" "X" "X" "O" "X"])})
			(let [request-for-play {:responder :playing :Cookie (merge helper/default-game (assoc helper/default-cookie :status :playing))}
						game (manager/manage-game request-for-play)]
				(should= :player2 (:current-player game))
				(should= ["X" "X" "O" "O" "O" "X" "X" "O" "X"] (:board game))
				(should= :game-over (:status game))
				(should= 0 (:winner game))))

		(it "to block a winning move"
			(reset! helper/mock-move 1)
			(reset! helper/games {-3141 (assoc helper/default-game :status :playing :current-player :player2 :board ["X" 1 "X" "O" 4 5 6 7 8])})
			(let [request-for-play {:responder :playing :Cookie (merge helper/default-game (assoc helper/default-cookie :status :playing))}
						game (manager/manage-game request-for-play)]
				(should= ["X" "O" "X" "O" 4 5 6 7 8] (:board game))
				(should-not= :game-over (:status game))
				(should-be-nil (:winner game))))
		)
	)



(ns game.computer-turns
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[spec-helper :as helper]
						[speclj.core :refer :all]
						[clojure.string :as str])
	(:import (server Router)))

(describe "Computer"

	(context "plays"
		(it "round 1 of game"
			;(swap! manager/games assoc 1 (assoc helper/default-game :gameID 1 :status :board-setup :board helper/empty-board :player2 {:type :human :piece "X" :player-num 2} :player2 {:type :human :piece "O" :player-num 1}))
			(let [request (assoc helper/request-map "resource" "/ttt/setup?board-size=3" "cookies" "oreo=1")
						response (walk/keywordize-keys (responder/create-response-map request))
						game (manager/manage-game request)]
				(should= :playing (:status game))
				(should= :player2 (:current-player game))
				(should= 1 (count (filter #(= "X" %) (:board game))))
				(should-not (:game-over game))
				(should-contain :re-route (keys response))))

		(it "vs computer for cat's game"
			;(swap! manager/game assoc :status :playing :board ["X" 1 "O" "O" "O" "X" "X" "O" "X"]
			;			 :current-player :player1 :users 0)
			(let [request (assoc helper/request-map "resource" "/ttt/playing/box=1")
						response (walk/keywordize-keys (responder/create-response-map request))
						game (manager/manage-game request)
						target (slurp (.getCanonicalPath (io/file "./tictactoe/game-over.html")))]
				(should= :player2 (:current-player game))
				(should= ["X" "X" "O" "O" "O" "X" "X" "O" "X"] (:board game))
				(should= :game-over (:status game))
				(should= 0 (:winner game))
				(should-contain :re-route (keys response))
				;(should-contain target (slurp (:body response)))
				))

		(it "to block a winning move"
			;(swap! manager/game assoc
			;			 :status :playing
			;			 :users 1
			;			 :board ["X" 1 "X" "O" 4 5 6 7 8]
			;			 :current-player :player2
			;			 :player2 {:type :computer :piece "O" :player-num 2}
			;			 :player1 {:type :human :piece " X " :player-num 1}
			;			 :winner nil)
			(let [request (assoc helper/request-map "resource" "/ttt/playing/box=0")
						response (walk/keywordize-keys (responder/create-response-map request))
						game (manager/manage-game request)
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= ["X" "O" "X" "O" 4 5 6 7 8] (:board game))
				(should-not= :game-over (:status game))
				(should-be-nil (:winner game))
				;(should-contain target (slurp (:body response)))
				(should-contain :re-route (keys response))))
		)
	)



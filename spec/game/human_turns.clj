(ns game.human-turns
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[spec-helper :as helper]
						[speclj.core :refer :all]))

(describe "Human"
	(before (reset! manager/game helper/default-game))

	(context "plays"
		(it "round 1 of game"
			(swap! manager/game assoc :status :playing :current-player :player1 :board helper/empty-board :player1 {:type :human :piece "X" :player-num 1})
			(let [request (assoc helper/request-map "resource" "/ttt/playing/box=4")
						response (walk/keywordize-keys (responder/create-response-map request))
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :player1 (:current-player game))
				(should= "X" (nth (:board game) 4))
				(should= "O" (nth (:board game) 0))
				(should-not (:game-over game))
				(should-contain target (slurp (:body response)))))

		(it "player1 wins"
			(swap! manager/game assoc :status :playing :current-player :player1 :board ["X" "X" 2 3 4 5 6 7 8] :player1 {:type :human :piece "X" :player-num 1} :player2 {:type :human :piece "O" :player-num 2})
			(let [request (assoc helper/request-map "resource" "/ttt/playing/box=2")
						response (walk/keywordize-keys (responder/create-response-map request))
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/game-over.html")))]
				(should= 1 (:winner game))
				(should= "X" (nth (:board game) 2))
				(should= :game-over (:status game))
				(should-contain target (slurp (:body response)))))

		)
	)
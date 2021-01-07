(ns game.human-turns
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[spec-helper :as helper]
						[server.starter :as starter]
						[speclj.core :refer :all]
						[clojure.string :as str]))

(describe "Human"
	(before-all (starter/start-server 2019 "tictactoe") (reset! manager/game helper/default-game))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "plays"
		(it "round 1 of game"
			(swap! manager/game assoc :status :playing :current-player :player1 :board helper/empty-board :player1 {:type :human :piece "X" :player-num 1})
			(let [response (client/get "http://localhost:2019/ttt/playing/box=4")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :player1 (:current-player game))
				(should= "X" (nth (:board game) 4))
				(should= "O" (nth (:board game) 0))
				(should-not (:game-over game))
				(should-contain target (:body response))))

		(it "player1 wins"
			(swap! manager/game assoc :status :playing :current-player :player1 :board ["X" "X" 2 3 4 5 6 7 8] :player1 {:type :human :piece "X" :player-num 1} :player2 {:type :human :piece "O" :player-num 2})
			(let [response (client/get "http://localhost:2019/ttt/playing/box=2")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/game-over.html")))]
				(should= :player1 (:current-player game))
				(should= "X" (nth (:board game) 2))
				(should (:game-over game))
				(should-contain target (:body response))))

		)
	)
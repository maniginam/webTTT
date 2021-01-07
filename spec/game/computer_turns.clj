(ns game.computer-turns
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[spec-helper :as helper]
						[server.starter :as starter]
						[speclj.core :refer :all]
						[clojure.string :as str]))

(describe "Computer"
	(before-all (starter/start-server 2018 "tictactoe") (reset! manager/game helper/default-game))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "plays"
		(it "round 1 of game"
			(swap! manager/game assoc :status :board-setup :board helper/empty-board :player2 {:type :human})
			(let [response (client/get "http://localhost:2018/ttt/setup?board-size=3")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :playing (:status game))
				(should= :player2 (:current-player game))
				(should= 1 (count (filter #(= "X" %) (:board game))))
				(should-not (:game-over game))
				(should-contain target (:body response))))

		(it "vs computer for cat's game"
			(swap! manager/game assoc :status :playing :board ["X" 1 "O" "O" "O" "X" "X" "O" "X"]
						 :current-player :player1 :users 0)
			(let [response (client/get "http://localhost:2018/ttt/playing/box=0")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :player2 (:current-player game))
				(should= ["X" "X" "O" "O" "O" "X" "X" "O" "X"] (:board game))
				(should= :game-over (:status game))
				(should= 0 (:winner game))
				(should-contain target (:body response))))

		(it "to block a winning move"
			(swap! manager/game assoc
						 :status :playing
						 :users 1
						 :board ["X" 1 "X" "O" 4 5 6 7 8]
						 :current-player :player2
						 :player2 {:type :computer :piece "O" :player-num 2}
						 :player1 {:type :human :piece " X " :player-num 1}
						 :winner nil)
			(let [response (client/get " http://localhost:2018/ttt/playing/box=0")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= ["X" "O" "X" "O" 4 5 6 7 8] (:board game))
				(should-not= :game-over (:status game))
				(should-be-nil (:winner game))
				(should-contain target (:body response))))

		(it "full game"
			(reset! manager/game helper/default-game)
			(swap! manager/game assoc :status :board-setup :users 0 :board helper/empty-board :level :hard)
			(let [response (client/get "http://localhost:2018/ttt/setup?board-size=3")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/game-over.html")))]
				(should= :game-over (:status game))
				(should= 9 (count (filter #(string? %) (:board game))))
				(should-contain target (:body response)))))
	)



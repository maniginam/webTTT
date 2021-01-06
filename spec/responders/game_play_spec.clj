(ns responders.game-play-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[spec-helper :as helper]
						[server.starter :as starter]
						[speclj.core :refer :all]))

(describe "TTT Game Play"
	(before-all (starter/start-server 2018 "tictactoe") (reset! manager/game helper/default-game))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "playing"

		(it "round 1 of game"
			(swap! manager/game assoc :status :ready-to-play :board helper/empty-board)
			(let [response (client/get "http://localhost:2018/ttt/playing")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :playing (:status game))
				(should= :player2 (:current-player game))
				(should= 1 (count (filter #(= "X" %) (:board game))))
				(should-not (:game-over game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "computer vs computer cat's game"
			(swap! manager/game assoc :status :playing :board ["X" 1 "O" "O" "O" "X" "X" "O" "X"]
						 :current-player :player1 :users 0)
			(let [response (client/get "http://localhost:2018/ttt/playing")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :player2 (:current-player game))
				(should= ["X" "X" "O" "O" "O" "X" "X" "O" "X"] (:board game))
				(should= :game-over (:status game))
				(should= 0 (:winner game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))


		(it "but computer does not let human win"
			(swap! manager/game assoc
						 :status :playing
						 :users 1
						 :board ["X" 1 "X" "O" 4 5 6 7 8]
						 :current-player :player2
						 :player1 {:type :human :piece "X" :player-num 1}
						 :winner nil)
			(let [response (client/get "http://localhost:2018/ttt/playing")
						game @manager/game
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= ["X" "O" "X" "O" 4 5 6 7 8] (:board game))
				(should-not= :game-over (:status game))
				(should-be-nil (:winner game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
		)
	)

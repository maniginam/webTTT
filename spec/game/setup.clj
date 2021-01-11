(ns game.setup
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[server.starter :as starter]
						[speclj.core :refer :all]))

(describe "Game Setup"
	(before-all (starter/start-server 1003 "tictactoe") (reset! manager/game manager/default-game))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "Continue last game?"
		(it "isn't allowed due to completed last-game"
			(swap! manager/game assoc :status :waiting :last-game {:status :game-over :board ["X" "X" "X" "X"]})
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup")]
				(should= :user-setup (:status @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "no"
			(swap! manager/game assoc :status :restart? :last-game {:status :playing :board [0 1 2 3]})
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup/continue=no")]
				(should= :user-setup (:status @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "yes"
			(swap! manager/game assoc :status :restart? :console :web :last-game {:status :playing :board [0 1 2 3] :console :web :current-player :player1 :player1 {:player-num 1 :piece "X" :type :human}})
			(let [response (client/get "http://localhost:1003/ttt/setup/continue=yes")
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :playing (:status @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
		)


	(it "starts setup"
		(swap! manager/game assoc :status :waiting)
		(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/continue?.html")))
					response (client/get "http://localhost:1003/ttt/setup")]
			(should= :restart? (:status @manager/game))
			(should-contain starter/server-name (get (:headers response) "Server"))
			(should-contain target (:body response))))

	(context "sets user-count with"
		(it "0 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup?users=0")]
				(should= 0 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "1 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/player-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup?users=1")]
				(should= 1 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "2 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/board-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup?users=2")]
				(should= 2 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
		)


	(context "sets players"
		(it "human is X"
			(reset! manager/game manager/default-game)
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup?piece=X")]
				(should= :human (get (:player1 @manager/game) :type))
				(should= :computer (get (:player2 @manager/game) :type))
				(should-contain target (:body response))))

		(it "human is O"
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						response (client/get "http://localhost:1003/ttt/setup?piece=O")]
				(should= :human (get (:player2 @manager/game) :type))
				(should= :computer (get (:player1 @manager/game) :type))
				(should-contain target (:body response))))
		)

	(it "sets level"
		(swap! manager/game assoc :status :level-setup)
		(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/board-setup.html")))
					response (client/get "http://localhost:1003/ttt/setup?level=easy")]
			(should= :easy (get @manager/game :level))
			(should-contain target (:body response))))

	(it "sets board"
		(swap! manager/game assoc :console :web :status :board-setup :users 2 :level :easy :player1 {:piece "X" :type :human :player-num 1} :player2 {:piece "O" :type :computer :player-num 2})
		(let [response (client/get "http://localhost:1003/ttt/setup?board-size=2")
					target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
			(should= :playing (get @manager/game :status))
			(should= [0 1 2 3] (get @manager/game :board))
			(should-contain target (:body response))))
	)

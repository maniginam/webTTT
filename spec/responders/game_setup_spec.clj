(ns responders.game-setup-spec
	(:require [clojure.java.io :as io]
						[clj-http.client :as client]
						[speclj.core :refer :all]
						[server.starter :as starter]
						[game.game-manager :as manager]
						))

(def default-game {:console :gui
									 :status         :waiting
									 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}
									 :users          nil
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type nil}
									 :player2        {:player-num 2 :piece "O" :type nil}})

(describe "Home Screen Form"
	(before-all (starter/start-server 1518 "testroot"))
	(after-all (starter/stop))

	;(context "sets user-count"

		(it "0 humans"
			(swap! manager/game assoc :status :waiting)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/level-setup.html")))
						response (client/get "http://localhost:1518/ttt/setup?users=0")]
				(should= 0 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "1 humans"
			(swap! manager/game assoc :status :waiting)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/player-setup.html")))
						response (client/get "http://localhost:1518/ttt/setup?users=1")]
				(should= 1 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "2 humans"
			(swap! manager/game assoc :status :waiting)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/board-setup.html")))
						response (client/get "http://localhost:1518/ttt/setup?users=2")]
				(should= 2 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
	;)

	;(context "sets players"

		(it "human is X"
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/level-setup.html")))
						response (client/get "http://localhost:1518/ttt/setup?player=X")]
				(should= :human (get (:player1 @manager/game) :type))
				(should= :computer (get (:player2 @manager/game) :type))
				(should-contain target (:body response))))

		(it "human is O"
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/level-setup.html")))
						response (client/get "http://localhost:1518/ttt/setup?player=O")]
				(should= :human (get (:player2 @manager/game) :type))
				(should= :computer (get (:player1 @manager/game) :type))
				(should-contain target (:body response))))
	;)

	(it "sets level"
		(swap! manager/game assoc :status :level-setup)
		(let [target (slurp (.getCanonicalPath (io/file "./testroot/board-setup.html")))
					response (client/get "http://localhost:1518/ttt/setup?level=easy")]
			(should= :easy (get @manager/game :level))
			(should-contain target (:body response))))

	(it "sets board"
		(swap! manager/game assoc :console :gui :status :board-setup :users 0 :level :easy :player1 {:piece "X" :type :computer :player-num 1} :player2 {:piece "O" :type :computer :player-num 2})
		(let [target (slurp (.getCanonicalPath (io/file "./testroot/ttt.html")))
					response (client/get "http://localhost:1518/ttt/setup?board-size=2")]
			(should= :playing (get @manager/game :status))
			(should= [0 1 2 3] (get @manager/game :board))
			(should-contain target (:body response))))
	)

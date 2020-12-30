(ns responders.game-setup-spec
	(:require [clojure.java.io :as io]
						[clj-http.client :as client]
						[speclj.core :refer :all]
						[server.starter :as starter]
						[game.game-manager :as manager]
						))

(def default-game {:status         :waiting
									 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}
									 :users          nil
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type nil}
									 :player2        {:player-num 2 :piece "O" :type nil}})

(describe "Home Screen Form"
	(before-all (starter/start-server 3141 "testroot"))
	(after-all (starter/stop) (Thread/sleep 1000))

	(context "sets user-count"
		(it "0 humans"
			(reset! manager/game default-game)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/level-setup.html")))
						response (client/get "http://localhost:3141/ttt/form?users=0")]
				(should= 0 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "1 humans"
			(reset! manager/game default-game)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/player-setup.html")))
						response (client/get "http://localhost:3141/ttt/form?users=1")]
				(should= 1 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))

		(it "2 humans"
			(reset! manager/game default-game)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/board-setup.html")))
						response (client/get "http://localhost:3141/ttt/form?users=2")]
				(should= 2 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response)))))

	(context "sets players"

		(it "human is X"
			(reset! manager/game default-game)
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/level-setup.html")))
						response (client/get "http://localhost:3141/ttt/form?player=X")]
				(should= "X" (get :piece  (:player-1 @manager/game)))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response)))))
	)
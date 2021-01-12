(ns server.starter_spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[server.starter :as starter]
						[speclj.core :refer :all]))

	(describe "Connection to Server"
		(before (starter/start-server 1518 "tictactoe"))
		(after (starter/stop))

		(it "splits options"
			(should= {:help "-h" :config "-x" :port 1518 :root "hello" :console "terminal"}
							 (starter/split-options (into-array ["-h" "-x" "-p" "1518" "-r" "hello" "terminal"]))))

		(context "responds with ttt index.html for"

			(it "blank request"
				(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/index.html")))
							response (client/get "http://localhost:1518")]
					(should-contain starter/server-name (get (:headers response) "Server"))
					(should= target, (:body response))))

			(it "/ request"
				(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/index.html")))
							response (client/get "http://localhost:1518/")]
					(should= target, (:body response))))

			(it "/ttt request"
				(reset! manager/game manager/default-game)
				(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/index.html")))
							response (client/get "http://localhost:1518/ttt")]
					(should= target, (:body response))))
			)
		)
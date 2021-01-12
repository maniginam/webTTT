(ns game.play-again
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[speclj.core :refer :all]
						[spec-helper :as helper]
						[server.starter :as starter]))

(describe "Play Again Response"
	(before (starter/start-server 7393 "tictactoe") (reset! manager/game helper/default-game))
	(after (starter/stop))

	(context "play-again?"
			(it "no thanks"
				(Thread/sleep 500)
				(let [response (client/get "http://localhost:7393/ttt/play-again")
							target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
							game @manager/game]
					(should= :user-setup (:status game))
					(should-contain target (:body response))))
		)
	)
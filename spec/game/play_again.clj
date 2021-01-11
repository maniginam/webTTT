(ns game.play-again
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[speclj.core :refer :all]
						[spec-helper :as helper]
						[server.starter :as starter]))

(describe "Play Again Response"
	(before-all (starter/start-server 7393 "tictactoe") (reset! manager/game helper/default-game))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "play-again?"
			(it "no thanks"
				(let [response (client/get "http://localhost:2018/ttt/play-again")
							target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
							game @manager/game]
					(should= :user-setup (:status game))
					(should-contain target (:body response))))
		)
	)
(ns responders.play-responder-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[server.starter :as starter]
						[speclj.core :refer :all]))

(describe "TTT Game Play"
	(before-all (starter/start-server 2018 "tictactoe"))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "playing"

		(it "game play begins"
			(swap! manager/game assoc :status :ready-to-play)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))
						response (client/get "http://localhost:2018/ttt/playing")]
				(should= 1 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
		)
	)
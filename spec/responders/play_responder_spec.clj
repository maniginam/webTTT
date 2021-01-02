(ns responders.play-responder-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[game.game-manager :as manager]
						[server.starter :as starter]
						[speclj.core :refer :all]))

(describe "TTT Game Play"
	(before-all (starter/start-server 2018 "testroot"))
	(after-all (if (> 0 (Thread/activeCount)) (starter/stop)))

	(context "game start"

		(it "First Turn"
			(swap! manager/game assoc :status :playing)
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/ttt.html")))
						response (client/get "http://localhost:2018/ttt/setup?board-size=3")]
				(should= 1 (:users @manager/game))
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target (:body response))))
		)
	)
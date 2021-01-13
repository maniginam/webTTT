(ns game.play-again
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[speclj.core :refer :all]
						[spec-helper :as helper]))

(describe "Play Again Response"
	(before (reset! manager/game helper/default-game))

	(context "play-again?"
			(it "no thanks"
				(let [request (assoc helper/request-map "resource" "/ttt/play-again")
							response (walk/keywordize-keys (responder/create-response-map request))
							target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
							game @manager/game]
					(should= :user-setup (:status game))
					(should-contain target (slurp (:body response)))))
		)
	)
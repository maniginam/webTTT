(ns game.cookies-spec
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[spec-helper :as helper]
						[speclj.core :refer :all]
						[clojure.string :as string]
						[master.core :as tcore]
						[master.game-master :as game]
						[clojure.string :as str])
	(:import (server Router)))

(defn set-request [box cookie]
	{:httpVersion "HTTP/1.1"
	 :resource (str "/ttt/playing/box=" box)
	 :method "GET"
	 :cookie (str "oreo=" cookie)}
  )

(describe "Cookies"
	;(before (reset! manager/game helper/default-game))

		(it "for rex"
			;(swap! manager/game assoc :gameID "rex")
			(let [request (assoc helper/request-map "resource" "/ttt/setup?board-size=3")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should (int? (Integer/parseInt (last (str/split (:cookie response) #"=")))))))

(it "contain gameID"
	;(swap! manager/game assoc :console :web :status :board-setup)
	(let [request (assoc helper/request-map "resource" "/ttt/setup?board-size=3")
				response (walk/keywordize-keys (responder/create-response-map request))]
		(should (int? (Integer/parseInt (last (str/split (:cookie response) #"=")))))))

	(it "initiates game at waiting"
		;(swap! manager/game assoc :console :web :status :waiting)
		(let [request (assoc helper/request-map "resource" "/ttt/setup")
					response (walk/keywordize-keys (responder/create-response-map request))]
			(should (int? (Integer/parseInt (last (str/split (:cookie response) #"=")))))))

	(it "plays two separate games"
		(let [game1 (assoc manager/default-game :console :web :status :playing
																		:current-player :player1
																		:player1 {:type :human :piece "X" :player-num 1}
																		:player2 {:type :human :piece "O" :player-num 2}
																		:board [0 1 2 3])
					game2 (assoc manager/default-game :console :web :status :playing
																		:current-player :player1
																		:player1 {:type :human :piece "X" :player-num 1}
																		:player2 {:type :computer :piece "O" :player-num 2}
																		:board [0 1 2 3 4 5 6 7 8])
					cookie1 (:gameID (game/start-game! game1))
					cookie2 (:gameID (game/start-game! game2))
					game1request1 (set-request 0 cookie1)
					game2request1 (set-request 0 cookie2)
					game1request2 (set-request 4 cookie1)
					game2request2 (set-request 2 cookie2)
					response11 (responder/prep-for-game game1request1)
					response21 (responder/prep-for-game game2request1)
					response12 (responder/prep-for-game game1request2)
					response22 (responder/prep-for-game game2request2)]

			(should= ["X" 1 2 3 4 5 6 7 8] (:board response11))
			(should= ["X" 1 2 3 "O" 5 6 7 8] (:board response21))
			(should= ["X" 1 2 3 "O" 5 6 7 8] (:board response12))
			(should= ["X" "O" "X" 3 "O" 5 6 7 8] (:board response22)))
		)
)
(ns game.cookies-spec
	(:require [clojure.string :as str]
						[game.game-manager :as manager]
						[master.game-master :as game]
						[responders.ttt-responder :as responder]
						[spec-helper :as helper]
						[speclj.core :refer :all]
						[clojure.walk :as walk]))

(defn set-request [cookie]
	{:httpVersion "HTTP/1.1"
	 :resource    (str "/ttt/playing/box=" @helper/mock-move)
	 :method      "GET"
	 :cookie      cookie}
	)

(describe "Cookies"

	(context "finds the right cookie"
		(it "nil cookie"
			(let [cookies "null"
						request {:Cookie cookies}]
				(should= nil (responder/eat-cookies request))))

		(it "1 nil & 1 cookie"
			(let [cookies "null; {:cookieID 1 :status :playing}"
						request {:Cookie cookies}]
				(should= {:cookieID 1 :status :playing} (responder/eat-cookies request))))

		(it "1 nil & 1 cookie of 1 & 1 cookie of 0.5"
			(let [cookies "null; {:cookieID 1 :status :playing}; {:cookieID 0.5 :status :board-setup}"
						request {:Cookie cookies}]
				(should= {:cookieID 1 :status :playing} (responder/eat-cookies request))))

		(it "1 nil & 1 cookie of 1 & 1 cookie of 18.6"
			(let [cookies "null; {:cookieID 1 :status :playing}; {:cookieID 18.6 :status :board-setup}"
						request {:Cookie cookies}]
				(should= {:cookieID 18.6 :status :board-setup} (responder/eat-cookies request))))

		(it "1 nil & 1 cookie of 1 & 1 cookie of 18.6 & 1 cookie with nil gameID"
			(let [cookies "null; {:cookieID 1 :status :playing}; {:cookieID 18.6 :status :board-setup} {:cookieID nil :status :blahblah}"
						request {:Cookie cookies}]
				(should= {:cookieID 18.6 :status :board-setup} (responder/eat-cookies request))))
		)

	(context "starts game in play after board-setup as"
	 (it "comp v comp"
		(reset! helper/mock-move 0)
		(let [request {:resource "/ttt/setup?board-size=2" :responder :setup :entry 2 :Cookie (str "null; " (assoc helper/default-cookie :status :board-setup :cookieID -3141))}
					response (responder/prep-for-game request)
					game (:game response)]
			(should= :playing (:status game))
			(should= ["X" 1 2 3] (:board game))
			(should (int? (:gameID game)))))

	(it "human v human"
		(reset! helper/mock-move 0)
		(let [request {:resource "/ttt/setup?board-size=2" :responder :setup :entry 2 :Cookie (str "null; " (assoc helper/default-cookie :status :board-setup :cookieID -3141 :player1 {:type :human  :player-num 1 :piece "X"}))}
					response (responder/prep-for-game request)
					game (:game response)]
			(should= :playing (:status game))
			(should= [0 1 2 3] (:board game))
			(should (int? (:gameID game))))))

(xit
	(it "plays two separate games"
		(reset! helper/games {-10319 (assoc helper/default-game :status :playing :board [0 1 2 3] :player1 {:player-num 1 :piece "X" :type :mock}
																																:player2        {:player-num 2 :piece "O" :type :mock})})
		(swap! helper/games assoc -10518 (assoc helper/default-game :status :playing :board [0 1 2 3 4 5 6 7 8] :player1        {:player-num 1 :piece "X" :type :mock}
																																:player2        {:player-num 2 :piece "O" :type :mock}))
		(let [cookie1 (assoc helper/default-cookie :persistence {:db :mock :dbname "ttt"} :cookieID -10319 :status :playing :board [0 1 2 3] :player1        {:player-num 1 :piece "X" :type :mock}
																							 :player2        {:player-num 2 :piece "O" :type :mock})
					cookie2 (assoc helper/default-cookie :persistence {:db :mock :dbname "ttt"} :cookieID -10518 :status :playing :board [0 1 2 3 4 5 6 7 8] :player1        {:player-num 1 :piece "X" :type :mock}
																							 :player2        {:player-num 2 :piece "O" :type :mock})
					game1request1 (do (reset! helper/mock-move 0) (set-request cookie1))
					game2request1 (set-request cookie2)
					game1request2 (do (reset! helper/mock-move 2) (set-request cookie1))
					game2request2 (do (reset! helper/mock-move 4) (set-request cookie2))
					response11 (manager/manage-game (assoc game1request1 :entry "0" :responder :playing :Cookie cookie1))
					response21 (manager/manage-game (assoc game2request1 :entry "0" :responder :playing :Cookie cookie2))
					response12 (manager/manage-game (assoc game1request2 :entry "2" :responder :playing :Cookie (assoc cookie1 :board ["X" 1 2 3])))
					response22 (manager/manage-game (assoc game2request2 :entry "4" :responder :playing :Cookie (assoc cookie2 :board ["X" 1 2 3 4 5 6 7 8])))]

			(should= ["X" 1 2 3] (:board response11))
			(should= ["X" 1 2 3 "O" 5 6 7 8] (:board response21))
			(should= ["X" 1 "O"] (:board response12))
			(should= ["X" "O" "X" 3 "O" 5 6 7 8] (:board response22)))
		))
	)
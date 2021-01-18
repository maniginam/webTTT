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

(it "initiates game at waiting"
		(let [request (assoc helper/request-map "resource" "/ttt/setup")
					response (walk/keywordize-keys (responder/create-response-map request))]
			(should-not-be-nil (:cookie response))))

(xit
	(it "plays two separate games"
		(reset! helper/games {-10319 (assoc helper/default-game :status :playing :board [0 1 2 3] :player1 {:player-num 1 :piece "X" :type :mock}
																																:player2        {:player-num 2 :piece "O" :type :mock})})
		(swap! helper/games assoc -10518 (assoc helper/default-game :status :playing :board [0 1 2 3 4 5 6 7 8] :player1        {:player-num 1 :piece "X" :type :mock}
																																:player2        {:player-num 2 :piece "O" :type :mock}))
		(let [cookie1 (assoc helper/default-cookie :persistence {:db :mock :dbname "ttt"} :gameID -10319 :status :playing :board [0 1 2 3] :player1        {:player-num 1 :piece "X" :type :mock}
																							 :player2        {:player-num 2 :piece "O" :type :mock})
					cookie2 (assoc helper/default-cookie :persistence {:db :mock :dbname "ttt"} :gameID -10518 :status :playing :board [0 1 2 3 4 5 6 7 8] :player1        {:player-num 1 :piece "X" :type :mock}
																							 :player2        {:player-num 2 :piece "O" :type :mock})
					game1request1 (do (reset! helper/mock-move 0) (set-request cookie1))
					game2request1 (set-request cookie2)
					game1request2 (do (reset! helper/mock-move 2) (set-request cookie1))
					game2request2 (do (reset! helper/mock-move 4) (set-request cookie2))
					response11 (manager/manage-game (assoc game1request1 :entry {:box "0"} :responder :playing :Cookie cookie1))
					response21 (manager/manage-game (assoc game2request1 :entry {:box "0"} :responder :playing :Cookie cookie2))
					response12 (manager/manage-game (assoc game1request2 :entry {:box "2"} :responder :playing :Cookie (assoc cookie1 :board ["X" 1 2 3])))
					response22 (manager/manage-game (assoc game2request2 :entry {:box "4"} :responder :playing :Cookie (assoc cookie2 :board ["X" 1 2 3 4 5 6 7 8])))]

			(should= ["X" 1 2 3] (:board response11))
			(should= ["X" 1 2 3 "O" 5 6 7 8] (:board response21))
			(should= ["X" 1 "O"] (:board response12))
			(should= ["X" "O" "X" 3 "O" 5 6 7 8] (:board response22)))
		))
	)
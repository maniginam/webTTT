(ns game.setup-spec
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[speclj.core :refer :all]
						[spec-helper :as helper]
						[clojure.string :as str]
						[master.core :as tcore]))

(describe "Game Setup"
	(with-stubs)
	(around [it] (with-redefs [tcore/load-game (stub :load-game)]
								 ))
	(context "start setup with"
		(it "completed last game"
			(reset! helper/games {:cookieID 1 :status :game-over :board ["X"]})
			(reset! manager/gameID -271828)
			(let [request (assoc helper/request-map "responder" "setup" "resource" "/ttt/setup")
						response (walk/keywordize-keys (responder/create-response-map request))
						game (get @manager/games -271827)]
				(should (= :user-setup (:status game)))
				(should-contain :Set-Cookie (keys response))
				(should-have-invoked :load-game {:with [game]})))

		(it "incomplete last game"
			(reset! helper/games {:cookieID 1 :status :playing :board [0] :current-player :player1 :player1 {:type :mock :piece "O" :playerNum 1}})
			(reset! manager/gameID -271828)
			(let [request (assoc helper/request-map "responder" "setup" "resource" "/ttt/setup")
						response (walk/keywordize-keys (responder/create-response-map request))
						game (get @manager/games -271827)]
				(should (= :restart? (:status game)))
				(should-contain :Set-Cookie (keys response))
				(should-have-invoked :load-game {:with [game]})))

		(it "\"setup\" request entry"
			(let [request (assoc helper/request-map :status :board-setup "responder" "setup" "resource" "/ttt/setup")
						game (manager/manage-game request)]
				(should (= :user-setup (:status game)))
				(should-have-invoked :load-game {:with [game]}))))

	(context "continue last game?"
		(it "no"
			(reset! helper/games {:cookieID 1 :status :playing :board [0] :current-player :player1 :player1 {:type :mock :piece "O" :playerNum 1}})
			(let [request {:entry "no" :responder :setup :Cookie (assoc helper/default-cookie :status :restart?)}
						game (manager/manage-game request)]
				(should= :user-setup (:status game))))

		(it "yes"
			(reset! helper/games {:cookieID 1 :status :playing :board [0] :current-player :player1 :player1 {:type :mock :piece "O" :playerNum 1}})
			(let [request {:entry "yes" :responder :setup :Cookie (assoc helper/default-cookie :status :restart?)}
						game (manager/manage-game request)]
				(should= :playing (:status game))
				(should= [0] (:board game))))
		)

	(context "sets"
		(context "users to"
			(it "0 humans"
				(let [request {:entry "0" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :cookieID -3141)}
							game (manager/manage-game request)]
					(should= :level-setup (:status game))
					(should= :computer (get (:player1 game) :type))
					(should= :computer (get (:player2 game) :type))
					(should= 0 (:users game))))

			(it "1 humans"
				(let [request {:entry "1" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :cookieID -3141)}
							game (manager/manage-game request)]
					(should= :player-setup (:status game))
					(should= 1 (:users game))))

			(it "2 humans"
				(let [request {:entry "2" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :cookieID -3141)}
							game (manager/manage-game request)]
					(should= :human (get (:player1 game) :type))
					(should= :human (get (:player2 game) :type))
					(should= :board-setup (:status game))
					(should= 2 (:users game))))
			)

		(context "human as"
		 (it "X"
			(let [request {:entry "X" :responder :setup :Cookie (assoc helper/default-cookie :status :player-setup :cookieID -3141)}
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= :human (get (:player1 game) :type))
				(should= :computer (get (:player2 game) :type))))

		(it "O"
			(let [request {:entry "O" :responder :setup :Cookie (assoc helper/default-cookie :status :player-setup :cookieID -3141)}
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= :human (get (:player2 game) :type))
				(should= :computer (get (:player1 game) :type)))))

		(it "level to easy"
			(let [request {:entry "easy" :responder :setup :Cookie (assoc helper/default-cookie :status :level-setup :cookieID -3141)}
						game (manager/manage-game request)]
				(should= :board-setup (:status game))
				(should= :easy (get game :level))))

		(it "board 2x2 board"
			(reset! helper/mock-move nil)
			(reset! helper/games {})
			(let [request {:entry "2" :responder :setup :Cookie (dissoc (assoc helper/default-cookie :status :board-setup :cookieID -3141) :board-size)}
						game (manager/manage-game request)]
				(println "(:gameID game): " (:gameID game))
				(should= :playing (get game :status))
				(should= [0 1 2 3] (get game :board))
				(should= 1 (:cookieID game))
				(should (int? (:gameID game)))))
		)
	)

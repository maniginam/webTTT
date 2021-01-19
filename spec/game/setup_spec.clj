(ns game.setup-spec
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[speclj.core :refer :all]
						[spec-helper :as helper]
						[clojure.string :as str]))

(def testID (atom 0))

(describe "Game Setup"
	;(before (reset! manager/game helper/default-game))

	(context "Continue last game?"
		(it "isn't allowed due to completed last-game"
			;(reset! manager/games {-3141 (assoc manager/default-game :status :waiting :gameID -3141 :cookie -3141)})
			(let [request {:entry nil :responder :setup :Cookie (assoc helper/default-cookie :status :waiting :last-game {:status :game-over :board ["X"]} :gameID -3141)}
						game (manager/manage-game request)]
				(should= :user-setup (:status game))))

		(it "no"
			;(reset! manager/games {-3141 (assoc manager/default-game :status :restart? :gameID -3141 :cookie -3141  :last-game {:status :playing :board [0]})})
			(let [request {:entry "no" :responder :setup :Cookie (assoc helper/default-cookie :status :restart? :last-game {:status :playing :board [0]} :gameID -3141)}
						game (manager/manage-game request)]
				(should= :user-setup (:status game))))

		(it "yes"
			;(reset! manager/games {-3141 (assoc manager/default-game :status :restart? :gameID -3141 :cookie -3141 :last-game {:status :playing :board [0]})})
			(let [request {:entry "yes" :responder :setup :Cookie (assoc helper/default-cookie :status :restart? :last-game {:status :playing :board [0]} :gameID -3141)}
						game (manager/manage-game request)]
				(should= :playing (:status game))
				(should= [0] (:board game))))
		)

	(it "starts setup"
		(reset! manager/gameID -271828)
		(let [request (assoc helper/request-map "responder" "setup" "resource" "/ttt/setup")
					response (walk/keywordize-keys (responder/create-response-map request))
					game (get @manager/games -271827)]
			(should (or (= :restart? (:status game)) (= :user-setup (:status game))))
			(should-contain :Set-Cookie (keys response))))

	(context "sets user-count with"
		(it "0 humans"
			(let [request {:entry "0" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :gameID -3141)}
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= 0 (:users game))))

		(it "1 humans"
			(let [request {:entry "1" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :gameID -3141)}
						game (manager/manage-game request)]
				(should= :player-setup (:status game))
				(should= 1 (:users game))))

		(it "2 humans"
			(let [request {:entry "2" :responder :setup :Cookie (assoc helper/default-cookie :status :user-setup :gameID -3141)}
						game (manager/manage-game request)]
				(should= :board-setup (:status game))
				(should= 2 (:users game))))
		)

	(context "sets players"
		(it "human is X"
			(let [request {:entry "X" :responder :setup :Cookie (assoc helper/default-cookie :status :player-setup :gameID -3141)}
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= :human (get (:player1 game) :type))
				(should= :computer (get (:player2 game) :type))))

		(it "human is O"
			(let [request {:entry "O" :responder :setup :Cookie (assoc helper/default-cookie :status :player-setup :gameID -3141)}
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= :human (get (:player2 game) :type))
				(should= :computer (get (:player1 game) :type))))
		)

	(it "sets level"
		(let [request {:entry "easy" :responder :setup :Cookie (assoc helper/default-cookie :status :level-setup :gameID -3141)}
					game (manager/manage-game request)]
			(should= :board-setup (:status game))
			(should= :easy (get game :level))))

	(it "sets board"
		(reset! helper/mock-move nil)
		(let [request {:entry "2" :responder :setup :Cookie (dissoc (assoc helper/default-cookie :status :board-setup :gameID -3141) :board-size)}
					game (manager/manage-game request)]
			(should= :playing (get game :status))
			(should= [0 1 2 3] (get game :board))))
	)

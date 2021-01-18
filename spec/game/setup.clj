(ns game.setup
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
			(reset! manager/games {-3141 (assoc manager/default-game :status :waiting :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry nil :status :waiting :last-game {:status :game-over :board ["X"]} :gameID -3141)
						game (manager/manage-game request)]
				(should= :user-setup (:status game))))

		(it "no"
			(reset! manager/games {-3141 (assoc manager/default-game :status :restart? :gameID -3141 :cookie -3141  :last-game {:status :playing :board [0]})})
			(let [request (assoc manager/default-game :responder :setup :entry {:continue "no"} :status :restart? :last-game {:status :playing :board [0]} :gameID -3141)
						game (manager/manage-game request)]
				(should= :user-setup (:status game))))

		(it "yes"
			(reset! manager/games {-3141 (assoc manager/default-game :status :restart? :gameID -3141 :cookie -3141 :last-game {:status :playing :board [0]})})
			(let [request (assoc manager/default-game :responder :setup :entry {:continue "yes"} :status :restart? :last-game {:status :playing :board [0]} :gameID -3141)
						game (manager/manage-game request)]
				(should= :playing (:status game))
				(should= [0] (:board game))))
		)

	(it "starts setup"
		(reset! manager/gameID -10320)
		(let [request (assoc helper/request-map "responder" "setup" "resource" "/ttt/setup")
					response (walk/keywordize-keys (responder/create-response-map request))
					game (get @manager/games -10319)]
			(should (or (= :restart? (:status game)) (= :user-setup (:status game))))
			(should-contain :re-route (keys response))
			(should-contain :cookie (keys response))))

	(context "sets user-count with"
		(it "0 humans"
			(reset! manager/games {-3141 (assoc manager/default-game :status :user-setup :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry {:users "0"} :status :user-setup :users 1 :gameID -3141)
						game (manager/manage-game request)]
				(should= :level-setup (:status game))
				(should= 0 (:users game))))

		(it "1 humans"
			(reset! manager/games {-3141 (assoc manager/default-game :status :user-setup :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry {:users "1"} :status :user-setup :users 1 :gameID -3141)
						game (manager/manage-game request)]
				(should= :player-setup (:status game))
				(should= 1 (:users game))))

		(it "2 humans"
			(reset! manager/games {-3141 (assoc manager/default-game :status :user-setup :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry {:users "2"} :status :user-setup :users 1 :gameID -3141)
						game (manager/manage-game request)]
				(should= :board-setup (:status game))
				(should= 2 (:users game))))
		)

	(context "sets players"
		(it "human is X"
			(reset! manager/games {-3141 (assoc manager/default-game :status :player-setup :users 1 :gameID -3141 :cookie -3141)})
			(reset! manager/games {-3141 (assoc manager/default-game :status :player-setup :users 1 :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry {:piece "X"} :status :player-setup :users 1 :gameID -3141)
						game (manager/manage-game request)]
				(should= :human (get (:player1 game) :type))
				(should= :computer (get (:player2 game) :type))))

		(it "human is O"
			(reset! manager/games {-3141 (assoc manager/default-game :status :player-setup :users 1 :gameID -3141 :cookie -3141)})
			(let [request (assoc manager/default-game :responder :setup :entry {:piece "O"} :status :player-setup :users 1 :gameID -3141)
						game (manager/manage-game request)]
				(println "HUMAN IS O game: " game)
				(should= :human (get (:player2 game) :type))
				(should= :computer (get (:player1 game) :type))))
		)

	(it "sets level"
		(reset! manager/games {-3141 (assoc manager/default-game :status :level-setup :users 1 :gameID -3141 :cookie -3141)})
		(let [request (assoc manager/default-game :responder :setup :entry {:level :easy} :status :level-setup :users 1 :gameID -3141)
					game (manager/manage-game request)]
			(should= :easy (get game :level))))

	(it "sets board"
		(swap! manager/games assoc -3141 (assoc manager/default-game :status :board-setup :users 1 :gameID -3141 :cookie -3141 :current-player :player1 :player1 {:piece "X" :player-num 1 :type :human} :player2 {:piece "O" :player-num 2 :type :human}))
		(let [request (assoc manager/default-game :responder :setup :entry {:board-size "2"} :status :board-setup :users 1 :gameID -3141)
					game (manager/manage-game request)]
			(should= :playing (get game :status))
			(should= [0 1 2 3] (get game :board))))
	)

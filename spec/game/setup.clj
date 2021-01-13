(ns game.setup
	(:require [clojure.java.io :as io]
						[clojure.walk :as walk]
						[game.game-manager :as manager]
						[responders.ttt-responder :as responder]
						[speclj.core :refer :all]
						[spec-helper :as helper]))

(describe "Game Setup"
	(before (reset! manager/game helper/default-game))

	(context "Continue last game?"
		(it "isn't allowed due to completed last-game"
			(swap! manager/game assoc :status :waiting :last-game {:status :game-over :board ["X" "X" "X" "X"]})
			(let [request (assoc helper/request-map "resource" "/ttt/setup")
						response (walk/keywordize-keys (responder/create-response-map request))
						target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))]
				(should= :user-setup (:status @manager/game))
				(should-contain target (slurp (:body response)))))

		(it "no"
			(swap! manager/game assoc :status :restart? :last-game {:status :playing :board [0 1 2 3]})
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/user-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup/continue=no")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= :user-setup (:status @manager/game))
				(should-contain target (slurp (:body response)))))

		(it "yes"
			(swap! manager/game assoc :status :restart? :console :web :last-game {:status :playing :board [0 1 2 3] :console :web :current-player :player1 :player1 {:player-num 1 :piece "X" :type :human}})
			(let [request (assoc helper/request-map "resource" "/ttt/setup/continue=yes")
						response (walk/keywordize-keys (responder/create-response-map request))
						target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
				(should= :playing (:status @manager/game))
				(should-contain target (slurp (:body response)))))
		)


	(it "starts setup"
		(swap! manager/game assoc :status :waiting :last-game {:status :playing :board [0 "X" 2 3 4 5 6 7 8]})
		(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/continue?.html")))
					request (assoc helper/request-map "resource" "/ttt/setup")
					response (walk/keywordize-keys (responder/create-response-map request))]
			(should= :restart? (:status @manager/game))
			(should-contain target (slurp (:body response)))))

	(context "sets user-count with"
		(it "0 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup?users=0")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= 0 (:users @manager/game))
				(should-contain target (slurp (:body response)))))

		(it "1 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/player-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup?users=1")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= 1 (:users @manager/game))
				(should-contain target (slurp (:body response)))))

		(it "2 humans"
			(swap! manager/game assoc :status :user-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/board-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup?users=2")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= 2 (:users @manager/game))
				(should-contain target (slurp (:body response)))))
		)


	(context "sets players"
		(it "human is X"
			(reset! manager/game manager/default-game)
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup?piece=X")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= :human (get (:player1 @manager/game) :type))
				(should= :computer (get (:player2 @manager/game) :type))
				(should-contain target (slurp (:body response)))))

		(it "human is O"
			(swap! manager/game assoc :users 1 :status :player-setup)
			(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/level-setup.html")))
						request (assoc helper/request-map "resource" "/ttt/setup?piece=O")
						response (walk/keywordize-keys (responder/create-response-map request))]
				(should= :human (get (:player2 @manager/game) :type))
				(should= :computer (get (:player1 @manager/game) :type))
				(should-contain target (slurp (:body response)))))
		)

	(it "sets level"
		(swap! manager/game assoc :status :level-setup)
		(let [target (slurp (.getCanonicalPath (io/file "./tictactoe/board-setup.html")))
					request (assoc helper/request-map "resource" "/ttt/setup?level=easy")
					response (walk/keywordize-keys (responder/create-response-map request))]
			(should= :easy (get @manager/game :level))
			(should-contain target (slurp (:body response)))))

	(it "sets board"
		(swap! manager/game assoc :console :web :status :board-setup :users 2 :level :easy :player1 {:piece "X" :type :human :player-num 1} :player2 {:piece "O" :type :computer :player-num 2})
		(let [request (assoc helper/request-map "resource" "/ttt/setup?board-size=2")
					response (walk/keywordize-keys (responder/create-response-map request))
					target (slurp (.getCanonicalPath (io/file "./tictactoe/ttt.html")))]
			(should= :playing (get @manager/game :status))
			(should= [0 1 2 3] (get @manager/game :board))
			(should-contain target (slurp (:body response)))))
	)

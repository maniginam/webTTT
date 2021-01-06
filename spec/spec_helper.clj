(ns spec-helper
	(:require [speclj.core :refer :all]
						[clojure.java.io :as io]))

(def default-game {:console        :web
									 :status         :waiting
									 :persistence    {:db :mysql :dbname "mysql" :table "ttt"}
									 :users          nil
									 :board-size     3
									 :current-player :player1
									 :player1        {:player-num 1 :piece "X" :type :computer}
									 :player2        {:player-num 2 :piece "O" :type :computer}})


(def empty-board [0 1 2 3 4 5 6 7 8])

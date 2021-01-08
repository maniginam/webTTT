(ns html.game-over-writer
	(:require [hiccup.core :as hiccup]
						[html.core :as hcore]
						[html.game-writer :as board]
						[clojure.java.io :as io]))

(def winners {0 "Cat's Game!" 1 "X Wins!" 2 "O Wins!"})

(defn make-replay-button []
	(hiccup/html
		[:form {:action "/ttt/play-again" :method "get"}
		 [:button {:type "submit" :formaction "/ttt/play-again" :formmethod "get"} "Let's Play Again!"]]))

(defn write-game-over [game]
	(let [winner (get winners (:winner game))]
		(str "<h2>Game Over: " winner "</h2>\n")))

(defn write-body [game]
	(str hcore/body-start
			 (write-game-over game)
			 (make-replay-button) "\n"
			 hcore/svg-start
			 (board/draw-board game)))

(defmethod hcore/write! :game-over [game]
	(let [body (write-body game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str hcore/head body close)
				path (.getCanonicalPath (io/file "./tictactoe/game-over.html"))]
		(spit path text))
	)

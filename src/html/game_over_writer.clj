(ns html.game-over-writer
	(:require [hiccup.core :as hiccup]
						[html.standard-lines :as std]
						[html.game-writer :as board]
						[clojure.java.io :as io]))

(def winners {0 "Cat's Game!" 1 "X Wins!" 2 "O Wins!"})

(defn make-replay-button []
	(hiccup/html
		[:form {:action "/ttt/setup" :method "get"}
		 [:button {:type "submit" :formaction "/ttt/setup" :formmethod "get"} "Let's Play Again!"]])
	)

(defn write-game-over [game]
	(let [winner (get winners (:winner game))]
		(str "<h2>Game Over: " winner "</h2>\n")))

(defmethod std/write! :game-over [game]
	(let [context (str std/head)
				h2 (write-game-over game)
				button (str (make-replay-button) "\n")
				board (board/draw-board game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str context h2 button std/body-start board close)
				path (.getCanonicalPath (io/file "./tictactoe/game-over.html"))]
		(spit path text))
	)

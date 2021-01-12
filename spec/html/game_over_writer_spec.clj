(ns html.game-over-writer-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[clojure.string :as str]
						[html.game-over-writer :as writer]
						[hiccup.core :as hiccup]
						[html.core :as hcore]
						[server.starter :as starter]
						[speclj.core :refer :all]))

(def head (str "<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">"))
(def h1 "<h1>Tic Tac Toe</h1>")

(describe "HTML Game Over:"

	(context "winner:"
		(it "cat's game"
			(hcore/write! {:status :game-over :board ["X" "O" "X" "X" "O" "O" "O" "X" "X"] :winner 0})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<h2>Game Over: Cat's Game!</h2>", lines)
				(should-contain "<form action=\"/ttt/play-again\" method=\"get\"><button formaction=\"/ttt/play-again\" formmethod=\"get\" type=\"submit\">Let's Play Again!</button></form>", lines)
				(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

		(it "x wins"
			(hcore/write! {:status :game-over :board ["X" "X" "X" "X" "O" "O" "O" "X" "X"] :winner 1})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<h2>Game Over: X Wins!</h2>", lines)
				(should-contain "<form action=\"/ttt/play-again\" method=\"get\"><button formaction=\"/ttt/play-again\" formmethod=\"get\" type=\"submit\">Let's Play Again!</button></form>", lines)
				(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

		(it "o wins"
			(hcore/write! {:status :game-over :board ["X" "O" "X" "X" "O" "O" "O" "O" "X"] :winner 2 :winning-line [1 4 7]})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<h2>Game Over: O Wins!</h2>", lines)
				(should-contain "<form action=\"/ttt/play-again\" method=\"get\"><button formaction=\"/ttt/play-again\" formmethod=\"get\" type=\"submit\">Let's Play Again!</button></form>" lines)
				(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>" lines)
				(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>" lines)
				(should-contain "<circle cx=\"50.0%\" cy=\"50.0%\" r=\"10.5%\" stroke=\"rgb(152 251 152)\" stroke-width=\"25\" fill=\"none\"/>" lines))))
	)
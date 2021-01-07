(ns html.game-over-writer-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[server.starter :as starter]
						[speclj.core :refer :all]
						[html.game-over-writer :as writer]
						[hiccup.core :as hiccup]
						[html.standard-lines :as std]))

(def head (str "<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">"))
(def h1 "<h1>Tic Tac Toe</h1>")

(describe "HTML Game Over:"

	(context "winner:"
		(it "cat's game"
			(std/write! {:status :game-over :board ["X" "O" "X" "X" "O" "O" "O" "X" "X"] :winner 0})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<h2>Game Over: Cat's Game!</h2>", lines)
				(should-contain (hiccup/html
													[:form {:action "/ttt/setup" :method "get"}
													 [:button {:type "submit" :formaction "/ttt/setup" :formmethod "get"} "Let's Play Again!"]]), lines)
				(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

		(it "x wins"
			(std/write! {:status :game-over :board ["X" "X" "X" "X" "O" "O" "O" "X" "X"] :winner 1})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<h2>Game Over: X Wins!</h2>", lines)
				(should-contain (hiccup/html
													[:form {:action "/ttt/setup" :method "get"}
													 [:button {:type "submit" :formaction "/ttt/setup" :formmethod "get"} "Let's Play Again!"]]), lines)
				(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))
		)

	(it "o wins"
		(std/write! {:status :game-over :board ["X" "O" "X" "X" "O" "O" "O" "O" "X"] :winner 2})
		(let [ttt (.getCanonicalPath (io/file "./tictactoe/game-over.html"))
					html (slurp ttt)
					lines (str/split-lines html)]
			(should-contain head, lines)
			(should-contain h1, lines)
			(should-contain "<h2>Game Over: O Wins!</h2>", lines)
			(should-contain (hiccup/html
												[:form {:action "/ttt/setup" :method "get"}
												 [:button {:type "submit" :formaction "/ttt/setup" :formmethod "get"} "Let's Play Again!"]]), lines)
			(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
			(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

	)
(ns html.game-writer-spec
	(:require [clj-http.client :as client]
						[clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[html.game-writer :as writer]                         ;multimethod
						[html.core :as std]
						[server.starter :as starter]
						[spec-helper :as helper]
						[speclj.core :refer :all]))

(def head (str "<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">"))
(def h1 "<h1>Tic Tac Toe</h1>")

(describe "HTML Transcriber"

	(context "On the Console:"
		(context "draws standard board"
			(it "that is blank"
				(std/write! {:status :playing :board-size 3 :board [0 1 2 3 4 5 6 7 8]})
				(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
							html (slurp ttt)
							lines (str/split-lines html)]
					(should-contain head, lines)
					(should-contain h1, lines)
					(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
					(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

			(it "with 1 X"
				(std/write! {:status :playing :board-size 3 :board [0 1 2 3 "X" 5 6 7 8] :current-player :player2 :player2 {:piece "O"}})
				(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
							html (slurp ttt)
							lines (str/split-lines html)]
					(should-contain head, lines)
					(should-contain h1, lines)
					(should-contain "<h2>O's Turn!</h2>" lines)
					(should-contain "<line x1=\"5.0%\" y1=\"35.0%\" x2=\"95.0%\" y2=\"35.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
					(should-contain "<rect x=\"35.0%\" y=\"35.0%\" width=\"30.0%\" height=\"30.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)
					(should-contain "<a href=\"/ttt/playing/box=4\">" lines))))

		(context "draws board of size"
			(it "2x2"
				(std/write! {:status :playing :board-size 2 :board [0 1 2 3]})
				(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
							html (slurp ttt)
							lines (str/split-lines html)]
					(should-contain head, lines)
					(should-contain h1, lines)
					(should-contain "<line x1=\"5.0%\" y1=\"50.0%\" x2=\"95.0%\" y2=\"50.0%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
					(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"45.0%\" height=\"45.0%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

			(it "4x4"
				(std/write! {:status :playing :board-size 4 :board [0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15]})
				(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
							html (slurp ttt)
							lines (str/split-lines html)]
					(should-contain head, lines)
					(should-contain h1, lines)
					(should-contain "<line x1=\"5.0%\" y1=\"27.5%\" x2=\"95.0%\" y2=\"27.5%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
					(should-contain "<rect x=\"5.0%\" y=\"5.0%\" width=\"22.5%\" height=\"22.5%\" fill=\"blue\" opacity=\"10%\"/>", lines)))))
	)
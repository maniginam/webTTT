(ns html.writer-spec
	(:require [html.writer :as writer]
						[speclj.core :refer :all]
						[clojure.java.io :as io]
						[clojure.string :as str]))

(def head (str "<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">"))

(def h1 "<h1>Tic Tac Toe</h1>")

(describe "HTML Transcriber"

	(context "On the Console:"

		(it "draws standard board"
			(writer/write {:board [0 1 2 3 4 5 6 7 8] :test "write1"})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<line x1=\"5%\" y1=\"35%\" x2=\"95%\" y2=\"35%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5%\" y=\"5%\" width=\"30%\" height=\"30%\" fill=\"blue\" opacity=\"10%\"/>", lines)
				)
			)

		(it "draws 2x2 board"
			(writer/write {:board [0 1 2 3] :test "write2"})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<line x1=\"5%\" y1=\"50%\" x2=\"95%\" y2=\"50%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"5%\" y=\"5%\" width=\"45%\" height=\"45%\" fill=\"blue\" opacity=\"10%\"/>", lines)))

		(it "draws standard board with 1 X"
			(writer/write {:board [0 1 2 3 "X" 5 6 7 8] :test "write3"})
			(let [ttt (.getCanonicalPath (io/file "./tictactoe/ttt.html"))
						html (slurp ttt)
						lines (str/split-lines html)]
				(should-contain head, lines)
				(should-contain h1, lines)
				(should-contain "<line x1=\"5%\" y1=\"35%\" x2=\"95%\" y2=\"35%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>", lines)
				(should-contain "<rect x=\"35%\" y=\"35%\" width=\"30%\" height=\"30%\" fill=\"blue\" opacity=\"10%\"/>", lines)
				(should-contain "<a href=\"/ttt/playing/box=4\">" lines)
				(should-contain "<text x=\"5%\" y=\"5%\" font-size=\"30\" fill=\"coral\">X</text>" lines)
				)
			)
		)
	)
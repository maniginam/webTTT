;(ns html.continue-writer
;	(:require [hiccup.core :as hiccup]
;						[html.core :as hcore]
;						[clojure.java.io :as io]))
;
;
;
;
;
;
;
;(defn write-body [game]
;	(let [last-game (:last-game game)
;				status (:status last-game)]
;	(str "<h1>Tic Tac Toe</h1>"
;			 "<h2>Do you want to pick up where you left off?</h2>"
;			 "<body>"
;			 (hiccup/html {:action "/ttt/continue?" :method "get"})
;			 (hiccup/html [:button {:type "submit" :formaction "/ttt/continue=true" :formmethod "get"} "Let's Play!"])
;			 (hiccup/html [:button {:type "submit" :formaction "/ttt/continue=false" :formmethod "get"} "Let's Play!"])
;			 "</form>")))
;
;(defmethod hcore/write! :restart? [game]
;	(let [body (write-body game)
;				close (str "</body>\n</html>")
;				text (apply str hcore/head body close)
;				path (.getCanonicalPath (io/file "./tictactoe/continue?.html"))]
;		(spit path text))
;	)

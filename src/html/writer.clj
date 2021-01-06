(ns html.writer
	(:require [clojure.java.io :as io]
						[master.core :as tcore]))

(def head (str "<html>\n<head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">\n</head>\n"))
(def body-start (str "<body>\n<h1>Tic Tac Toe</h1>\n"
										 "<svg width=\"100%\" height=\"100%\">\n"))

(defn draw-board [game]
	(let [board (:board game)
				boxes-per-row (int (Math/sqrt (count board)))
				box-size (/ 90 boxes-per-row)
				min 5
				max 95
				lines (for [line (range 1 boxes-per-row)
										:let [constant (+ min (* line box-size))]]
								(str "<line x1=\"" min "%\" y1=\"" constant "%\" x2=\"" max "%\" y2=\"" constant "%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>\n"
										 "<line x1=\"" constant "%\" y1=\"" min "%\" x2=\"" constant "%\" y2=\"" max "%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>\n"))
				boxes (for [box (range 0 (count board))
										:let [x (+ min (* box-size (int (/ box boxes-per-row))))
													y (+ min (* box-size (rem box boxes-per-row)))
													x-text 5
													y-text 5
													text (if (string? (nth board box)) (nth board box) "")
													marker (cond (= "X" (nth board box)) {:x1 (+ x (* box-size 0.05)) :x2 (+ x (* box-size 0.95)) :y1 (+ y (* box-size 0.05)) :y2 (+ y (* box-size 0.95))}
																			 (= "O" (nth board box)) {:center-x (/ (- box-size x) 2) :center-y (/ (- box-size y) 2) :r (* box-size 0.9)}
																			 :else "")
													shape (cond (= "X" (nth board box)) (str "<line x1=\"" (:x1 marker) "\" y1=\"" (:y1 marker) "\" x2=\"\"" (:x2 marker) "\" y2=\"" (:y2 marker) "\" stroke=\"coral\" stroke-width=\"10\"/>\n"
																																	 "<line x1=\"" (:x2 marker) "\" y1=\"" (:y1 marker) "\" x2=\"\"" (:x1 marker) "\" y2=\"" (:y2 marker) "\" stroke=\"coral\" stroke-width=\"10\"/>\n")
																			(= "O" (nth board box)) (str "<circle cx=\"" (:center-x marker) "\" cy=\"" (:center-y marker) "\" r=\"" (:r marker) "\" y2=\"" (:y2 marker) "\" stroke=\"coral\" stroke-width=\"10\"/>\n")
																																	 :else nil)]]
								(str "<a href=\"/ttt/playing/box=" box "\">\n"
										 "<g>\n"
										 "<rect x=\"" x "%\" y=\"" y "%\" width=\"" box-size "%\" height=\"" box-size "%\" fill=\"blue\" opacity=\"10%\"/>\n"
										 ;"<text x=\"" x-text "%\" y=\"" y-text "%\" font-size=\"30\" fill=\"coral\">" text "</text>\n"
										 shape
										 "</g>"
										 "</a>\n"))]
		(apply str (apply str lines) boxes)))


(defn write [game]
	(println "(:board game): " (:board game))
	(let [context (str head body-start)
				board (draw-board game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str context board close)
				path (.getCanonicalPath (io/file "./tictactoe/ttt.html"))]
		(spit path text)
		)
	)


(defmethod tcore/draw-state :web [game]
	(write game))

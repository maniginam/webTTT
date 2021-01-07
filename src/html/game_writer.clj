(ns html.game-writer
	(:require [clojure.java.io :as io]
						[master.core :as tcore]
						[html.standard-lines :as std]))

(def head (str "<html>\n<head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">\n</head>\n"))
(def body-start (str "<body>\n<h1>Tic Tac Toe</h1>\n"
										 "<svg width=\"100%\" height=\"100%\">\n"))
(def box-fill "blue")
(def box-opacity "10%")
(def marker-color "coral")
(def o-width "25")
(def x-width "30")

(defn draw-X [specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				min-margin (* box-size 0.2)
				max-margin (* box-size 0.8)
				points {:x1 (+ x min-margin) :x2 (+ x max-margin) :y1 (+ y min-margin) :y2 (+ y max-margin)}]
		(str "<line x1=\"" (:x1 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x2 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" marker-color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n"
				 "<line x1=\"" (:x2 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x1 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" marker-color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n")))

(defn draw-O [specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				ratio 0.7
				points {:cx (+ x (/ box-size 2)) :cy (+ y (/ box-size 2)) :r (/ (* box-size ratio) 2)}]
		(str "<circle cx=\"" (:cx points) "%\" cy=\"" (:cy points) "%\" r=\"" (:r points) "%\" stroke=\"" marker-color "\" stroke-width=\"" o-width "\" fill=\"none\"/>\n")))

(defn write-box-strings [specs]
		(str "<a href=\"/ttt/playing/box=" (:box specs) "\">\n"
			 "<g>\n"
			 "<rect x=\"" (:x specs) "%\" y=\"" (:y specs) "%\" width=\"" (:box-size specs) "%\" height=\"" (:box-size specs) "%\" fill=\"" box-fill "\" opacity=\"" box-opacity "\"/>\n"
			 (:marker specs)
			 "</g>"
			 "</a>\n"))

(defn determine-marker [specs]
	(let [board (:board specs)
				box (:box specs)]
		(cond (= "X" (nth board box)) (draw-X specs)
					(= "O" (nth board box)) (draw-O specs)
					:else nil)))

(defn write-boxes [specs]
	(let [box-size (:box-size specs)
				boxes-per-row (:boxes-per-row specs)]
		(for [box (range 0 (count (:board specs)))
					:let [x (+ (:min specs) (* box-size (rem box boxes-per-row)))
								y (+ (:min specs) (* box-size (int (/ box boxes-per-row))))
								marker (determine-marker (assoc specs :x x :y y :box box))]]
			(write-box-strings (assoc specs :x x :y y :box box :marker marker)))))

(defn write-lines [boxes-per-row box-size min]
	(for [line (range 1 boxes-per-row)
				:let [constant (+ min (* line box-size))]]
		(str "<line x1=\"" min "%\" y1=\"" constant "%\" x2=\"" (- 100 min) "%\" y2=\"" constant "%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>\n"
				 "<line x1=\"" constant "%\" y1=\"" min "%\" x2=\"" constant "%\" y2=\"" (- 100 min) "%\" stroke=\"rgb(94, 94, 99)\" stroke-width=\"4\"/>\n")))

(defn draw-board [game]
	(let [board (:board game)
				boxes-per-row (int (Math/sqrt (count board)))
				box-size (float (/ 90 boxes-per-row))
				min (float 5)
				lines (apply str (write-lines boxes-per-row box-size min))
				boxes (write-boxes {:box-size box-size :board board :boxes-per-row boxes-per-row :min min})]
		(apply str lines boxes)))


(defmethod std/write! :playing [game]
	(let [context (str head body-start)
				board (draw-board game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str context board close)
				path (.getCanonicalPath (io/file "./tictactoe/ttt.html"))]
		(spit path text)))

(defmethod tcore/draw-state :web [game]
	(std/write! game))

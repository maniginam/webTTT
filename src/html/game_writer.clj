(ns html.game-writer
	(:require [clojure.java.io :as io]
						[master.core :as tcore]
						[html.core :as hcore]))

(def box-fill "blue")
(def box-opacity "10%")
(def marker-color "coral")
(def o-width "25")
(def x-width "30")

(defn draw-X [box specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				min-margin (* box-size 0.2)
				max-margin (* box-size 0.8)
				color (cond (.contains (:winner specs) box) "rgb(152 251 152)"
										:else marker-color)
				points {:x1 (+ x min-margin) :x2 (+ x max-margin) :y1 (+ y min-margin) :y2 (+ y max-margin)}]
		(str "<line x1=\"" (:x1 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x2 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n"
				 "<line x1=\"" (:x2 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x1 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n")))

(defn draw-O [box specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				ratio 0.7
				color (cond (.contains (:winner specs) box) "rgb(152 251 152)"
										:else marker-color)
				points {:cx (+ x (/ box-size 2)) :cy (+ y (/ box-size 2)) :r (/ (* box-size ratio) 2)}]
		(str "<circle cx=\"" (:cx points) "%\" cy=\"" (:cy points) "%\" r=\"" (:r points) "%\" stroke=\"" color "\" stroke-width=\"" o-width "\" fill=\"none\"/>\n")))

(defn determine-marker [box specs]
	(let [board (:board specs)]
		(cond (= "X" (nth board box)) (draw-X box specs)
					(= "O" (nth board box)) (draw-O box specs)
					:else nil)))

(defn write-box-strings [specs]
	(str "<a href=\"/ttt/playing/box=" (:box specs) "\">\n"
			 "<g>\n"
			 "<rect x=\"" (:x specs) "%\" y=\"" (:y specs) "%\" width=\"" (:box-size specs) "%\" height=\"" (:box-size specs) "%\" fill=\"" box-fill "\" opacity=\"" box-opacity "\"/>\n"
			 (determine-marker (:box specs) specs)
			 "</g>"
			 "</a>\n"))

(defn write-boxes [specs]
	(let [box-size (:box-size specs)
				boxes-per-row (:boxes-per-row specs)]
		(for [box (range 0 (count (:board specs)))
					:let [x (+ (:min specs) (* box-size (rem box boxes-per-row)))
								y (+ (:min specs) (* box-size (int (/ box boxes-per-row))))]]
			(write-box-strings (assoc specs :x x :y y :box box)))))

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
				winning-boxes (vec (:winning-line game))
				lines (apply str (write-lines boxes-per-row box-size min))
				boxes (write-boxes {:box-size box-size :board board :boxes-per-row boxes-per-row :min min :winner winning-boxes})]
		(apply str lines boxes)))


(defmethod hcore/write! :playing [game]
	(let [context (str hcore/head hcore/body-start hcore/svg-start)
				board (draw-board game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str context board close)
				path (.getCanonicalPath (io/file "./tictactoe/ttt.html"))]
		(spit path text)))

(defmethod tcore/draw-state :web [game]
	(hcore/write! game))

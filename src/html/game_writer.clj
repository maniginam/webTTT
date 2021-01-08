(ns html.game-writer
	(:require [html.core :as hcore]
						[clojure.java.io :as io]
						[master.core :as tcore]))

(def box-fill "blue")
(def box-opacity "10%")
(def playing-color "coral")
(def winner-color "rgb(152 251 152)")
(def cats-color "rgb(80 80 80)")
(def o-width "25")
(def x-width "30")

(defn draw-X [specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				min-margin (* box-size 0.2)
				max-margin (* box-size 0.8)
				color (:color specs)
				points {:x1 (+ x min-margin) :x2 (+ x max-margin) :y1 (+ y min-margin) :y2 (+ y max-margin)}]
		(str "<line x1=\"" (:x1 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x2 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n"
				 "<line x1=\"" (:x2 points) "%\" y1=\"" (:y1 points) "%\" x2=\"" (:x1 points) "%\" y2=\"" (:y2 points) "%\" stroke=\"" color "\" stroke-width=\"" x-width "\" stroke-linecap=\"round\"/>\n")))

(defn draw-O [specs]
	(let [x (:x specs)
				y (:y specs)
				box-size (:box-size specs)
				ratio 0.7
				color (:color specs)
				points {:cx (+ x (/ box-size 2)) :cy (+ y (/ box-size 2)) :r (/ (* box-size ratio) 2)}]
		(str "<circle cx=\"" (:cx points) "%\" cy=\"" (:cy points) "%\" r=\"" (:r points) "%\" stroke=\"" color "\" stroke-width=\"" o-width "\" fill=\"none\"/>\n")))

(defn determine-marker [specs]
	(let [board (:board specs)
				box (:box specs)
				color (cond (or (nil? (:winner specs)) (nil? (:winning-boxes specs))) playing-color
										(and (= :game-over (:status specs)) (zero? (:winner specs))) cats-color
										(.contains (:winning-boxes specs) box) winner-color
										:else playing-color)]
		(cond (= "X" (nth board box)) (draw-X (assoc specs :color color))
					(= "O" (nth board box)) (draw-O (assoc specs :color color))
					:else nil)))

(defn write-box-strings [specs]
	(str "<a href=\"/ttt/playing/box=" (:box specs) "\">\n"
			 "<g>\n"
			 "<rect x=\"" (:x specs) "%\" y=\"" (:y specs) "%\" width=\"" (:box-size specs) "%\" height=\"" (:box-size specs) "%\" fill=\"" box-fill "\" opacity=\"" box-opacity "\"/>\n"
			 (determine-marker specs)
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
				winner (:winner game)
				lines (apply str (write-lines boxes-per-row box-size min))
				boxes (write-boxes {:status (:status game) :box-size box-size :board board :boxes-per-row boxes-per-row :min min :winning-boxes winning-boxes :winner winner})]
		(apply str lines boxes)))


(defn write-player-line [player]
	(str "<h2>" (:piece player) "'s Turn!</h2>\n")
	)

(defmethod hcore/write! :playing [game]
	(let [player-line (write-player-line (get game (get game :current-player)))
				board (draw-board game)
				close (str "</svg>\n</body>\n</html>")
				text (apply str hcore/head hcore/body-start player-line hcore/svg-start board close)
				path (.getCanonicalPath (io/file "./tictactoe/ttt.html"))]
		(spit path text)))

(defmethod tcore/draw-state :web [game]
	(hcore/write! game))

(ns html.draw
	(:require [html.core :as hcore]
						[html.game-writer] ;multimethod
						[html.game-over-writer] ;multimethod
						[master.core :as tcore]))

(defmethod tcore/draw-state :web [game]
	(hcore/write! game))
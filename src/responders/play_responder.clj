(ns responders.play-responder
	(:require [clojure.java.io :as io]
						[game.game-manager :as manager]
						[gui.gui :as gui]
						[gui.gui-core :as gcore]
						[master.core :as tcore]
						[master.game-master :as game]
						[quil.middleware :as m]
						[quil.core :as q]
						))
;
;
;(defmethod responders.core/respond :playing [request]
;	(swap! manager/game assoc :status :playing)
;	(game/update-state @manager/game)
;	(let [body (slurp (str (:root request) "/ttt.html"))
;				size (count body)
;				response {"Server"         (:server-name request)
;									"statusCode"     (int 200)
;									"Content-Type"   "text/html"
;									"body"           (.getBytes body)
;									"Content-Length" size}]
;		(q/sketch :host "tictactoe"
;							:title "tictactoe"
;							:size [700 800]
;							:setup gcore/setup-gui
;							:update game/update-state
;							:draw tcore/draw-state
;							:mouse-clicked gcore/mouse-clicked
;							:key-typed gcore/key-typed
;							:features [:keep-on-top]
;							:middleware [m/fun-mode])
;		response)
;	)
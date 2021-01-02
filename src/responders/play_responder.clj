(ns responders.play-responder
	(:require [quil.core :as q]
						[quil.middleware :as m]
						[gui.gui :as gui]
						[master.core :as tcore]
						[master.game-master :as gm]
						[gui.gui-core :as gcore]
						[game.game-manager :as manager]
						[clojure.java.io :as io]))


(defmethod responders.core/respond :playing [request]
	(let [game @manager/game
				body (slurp (.getCanonicalPath (io/file (str "./" (:root request) "/ttt.html"))))
				size (count body)
				response {"Server"         (:server-name request)
									"statusCode"     (int 200)
									"Content-Type"   "application/octet-stream"
									"body"           (.getBytes body)
									"Content-Length" size}]
		(q/sketch :host "tictactoe"
							:title "tictactoe"
							:size [700 800]
							:setup gcore/setup-gui
							:update gm/update-state
							:draw tcore/draw-state
							:mouse-clicked gcore/mouse-clicked
							:key-typed gcore/key-typed
							:features [:keep-on-top]
							:middleware [m/fun-mode])
		response)
	)
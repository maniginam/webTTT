(ns responders.play-responder
	(:require [clojure.java.io :as io]
						[game.game-manager :as manager]
						[master.core :as tcore]
						[master.game-master :as game]
						[html.writer :as writer]))


(defmethod responders.core/respond :playing [request]
	(swap! manager/game assoc :status :playing)
	(reset! manager/game (game/update-state @manager/game))
	(let [body (slurp (str (:root request) "/ttt.html"))
				size (count body)
				response {"Server"         (:server-name request)
									"statusCode"     (int 200)
									"Content-Type"   "text/html"
									"body"           (.getBytes body)
									"Content-Length" size}]
		response)
	)
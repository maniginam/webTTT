;(ns responders.play-responder
;	(:require [clojure.java.io :as io]
;						[clojure.string :as str]
;						[game.game-manager :as manager]
;						[html.game-over-writer :as over]
;						[html.game-writer :as writer]
;						[master.core :as tcore]
;						[master.game-master :as game]
;						[responders.core :as rcore]))
;
;(defn play-turn [resource]
;	(if (not (game/ai-turn? game))
;		(let [box (Integer/parseInt (last (str/split resource #"=")))]
;			(reset! manager/game (game/update-game-with-move! game box))
;			(reset! manager/game (game/update-state game))
;			(if (= :game-over (:status game)) (html.core/write! game)))
;		(while (and (not (game/game-over? game)) (game/ai-turn? game))
;			(reset! manager/game (game/update-state game)))))
;
;(defmethod rcore/respond :playing [request]
;	(play-turn (:resource request))
;	(let [body (if (= :playing (:status game))
;							 (slurp (str (:root request) "/ttt.html"))
;							 (slurp (str (:root request) "/game-over.html")))
;				size (count body)
;				response {"Server"         (:server-name request)
;									"statusCode"     (int 200)
;									"Content-Type"   "text/html"
;									"body"           (.getBytes body)
;									"Content-Length" size}]
;		response)
;	)
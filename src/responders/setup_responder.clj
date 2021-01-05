(ns responders.setup-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[responders.core :as rcore]))

(def file-map {:user-setup  "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html"})

(defn extract-game [request]
		(let [target (str (first (:target request)))
					entries (str/split (second (str/split target #"\?")) #"&")
					entriesMap (into {} (map #(assoc {} (keyword (first %)) (second %)) (map #(str/split % #"=") entries)))]
			(manager/manage-game request entriesMap))
		)

(defmethod rcore/respond :setup [request]
	(if (= :waiting (:status @manager/game))
		(manager/manage-game request nil)
		(extract-game request))
	(let [game @manager/game
				body (slurp (str (:root request) (get file-map (:status game))))
				size (count body)
				response
				{"Server"         (:server-name request)
				 "statusCode"     (int 200)
				 "Content-Type"   "text/html"
				 "body"           (.getBytes body)
				 "Content-Length" size}]
		response))





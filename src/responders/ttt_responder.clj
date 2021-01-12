(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager])
	(:import (httpServer HttpResponseBuilder)
					 (server Responder)
					 (java.util Map HashMap$HashIterator HashMap)))

(def file-map {:waiting     "/index.html" :restart? "/continue?.html" :user-setup "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html" :playing "/ttt.html" :game-over "/game-over.html"})
;; TODO - GLM : File Rsponder already serves files--don't dulplicate, let him do that
(defn update-game-response [request]
	(let [body (slurp (str (:root request) (get file-map (:status @manager/game))))
				size (count body)
				response {"Server"         (:server-name request)
									"statusCode"     (int 200)
									"Content-Type"   "text/html"
									"body"           (.getBytes body)
									"Content-Length" size}]
		response))

(defn expand-java-map [request]
	(loop [keys (.keySet request)
				 key (first keys)
				 requestMap {}]
		(if (empty? keys)
			requestMap
			(let [requestMap (assoc requestMap (keyword key) (.get request key))]
				(recur (rest keys) (first (drop 1 keys)) requestMap)))))

(defn extract-game-entry [request]
	(let [entries (mapcat #(str/split % #"=") (mapcat #(str/split % #"&") (str/split (str (last (:target request))) #"\?")))
				key (if (= 2 (count entries)) (keyword (first entries)) (keyword (second entries)))
				entry-map (assoc {} key (last entries))]
		entry-map))

(defn parse-request-for-game [request]
	(let [crude-resource (:resource request)
				split-resource (remove empty? (str/split crude-resource #"/"))]
		(when (> (count split-resource) 1)
			(let [target (rest split-resource)
						type (keyword (first (str/split (first target) #"\?")))
						requestMap (assoc request :responder type :target target)
						entry (extract-game-entry requestMap)]
				(assoc requestMap :entry entry)))))

(defn not-nil-parse [requestMap]
	(let [parsed-request (parse-request-for-game requestMap)]
		(manager/manage-game parsed-request))
	@manager/game)

(defn nil-parse [requestMap]
	(let [request (assoc requestMap :entry nil)]
		(manager/manage-game request))
	@manager/game)

(defn home? [resource]
	(or (nil? resource) (= "/ttt" resource)))

(deftype TTTResponder [server-map]
	Responder
	(respond [this request]
		(let [requestMap (merge (expand-java-map request) (select-keys server-map [:root]))]
			(if (home? (:resource requestMap))
				(nil-parse requestMap)
				(not-nil-parse requestMap)))))

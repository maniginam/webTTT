(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager])
	(:import (httpServer HttpResponseBuilder)
					 (server Responder)))

(def file-map {:waiting     "/index.html" :restart? "/continue?.html" :user-setup "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html" :playing "/ttt.html" :game-over "/game-over.html"})

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
	(let [target (str (first (:target request)))
				entries (mapcat #(str/split % #"=") (mapcat #(str/split % #"&") (str/split (str (last (:target request))) #"\?")))
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

(deftype TTTResponder [server-map]
	Responder
	(respond [this request]
		(let [builder (new HttpResponseBuilder)
					root (:root server-map)
					requestMap (assoc (expand-java-map request) :root root :server-name (:server-name server-map))
					crude-resource (:resource requestMap)]
			(if (or (nil? crude-resource) (= "/ttt" crude-resource))
				(.buildResponse builder (update-game-response requestMap))
				(let [parsed-request (parse-request-for-game requestMap)]
					(manager/manage-game parsed-request)
					(.buildResponse builder (update-game-response parsed-request)))))))

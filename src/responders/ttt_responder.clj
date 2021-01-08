(ns responders.ttt-responder
	(:require [clojure.string :as str]
						[game.game-manager :as manager]
						[responders.core :as rcore]
						[responders.play-responder :as play]
						[clojure.java.io :as io])
	(:import (httpServer HttpResponseBuilder)
					 (server Responder))
	)

(def file-map {:user-setup  "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html" :playing "/ttt.html" :game-over "/game-over.html"})

(defn expand-java-map [request]
	(loop [keys (.keySet request)
				 key (first keys)
				 requestMap {}]
		(if (empty? keys)
			requestMap
			(let [requestMap (assoc requestMap (keyword key) (.get request key))]
				(recur (rest keys) (first (drop 1 keys)) requestMap)))))

(defn respond-with-home-page [request]
	(let [resource "/index.html"
				body (slurp (str (:root request) resource))
				size (count body)
				response {"Server"         (:server-name request)
									"statusCode"     (int 200)
									"Content-Type"   "text/html"
									"body"           (.getBytes body)
									"Content-Length" size}]
		response))

(defn build-response-map [request]
	(let [crude-resource (:resource request)
				split-resource (remove empty? (str/split crude-resource #"/"))]
		(if (= 1 (count split-resource))
			(respond-with-home-page request)
			(let [target (rest split-resource)
						type (keyword (first (str/split (first target) #"\?")))]
				(rcore/respond (assoc request :responder type :target target))))
		)
	)

(deftype TTTResponder [server-map]
	Responder
	(respond [this request]
		(let [builder (new HttpResponseBuilder)
					root (:root server-map)
					requestMap (expand-java-map request)
					mapResource (:resource requestMap)
					response (build-response-map (assoc requestMap :root root :server-name (:server-name server-map)))]
			(if (nil? response)
				(.buildResponse builder (build-response-map (assoc requestMap :root root :server-name (:server-name server-map))))
				(.buildResponse builder response)))))

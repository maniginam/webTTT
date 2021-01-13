(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[clj-http.client :as client])
	(:import (server Responder)
					 (httpServer FileResponder)))

(def root (atom (str (.getCanonicalPath (io/file "./tictactoe")))))

(def file-map {:waiting     "/index.html" :restart? "/continue?.html" :user-setup "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html" :playing "/ttt.html" :game-over "/game-over.html"})

;; TODO - GLM : File Rsponder already serves files--don't dulplicate, let him do that
(defn update-game-response [request]
	(let [body (slurp (str @root (get file-map (:status @manager/game))))
				size (count body)
				response {"statusCode"     (int 200)
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

(defn send-new-request [request]
	(let [request-string (str "http://" (:Host request) (get file-map (:status @manager/game)))]
		(update-game-response request)
	))

(defn not-home-parse [requestMap]
	(let [parsed-request (parse-request-for-game requestMap)]
		(manager/manage-game parsed-request)
		(update-game-response parsed-request)
		;(send-new-request requestMap)
		))

(defn home-no-parse [requestMap]
	(let [request (assoc requestMap :entry nil)]
		(manager/manage-game request)
		(update-game-response request)
		;(send-new-request request)
	))

(defn home? [resource]
	(or (nil? resource) (= "/ttt" resource)))

(defn create-response-map [request]
	(let [request-map (expand-java-map request)
				response-map (if (home? (:resource request-map))

											 (home-no-parse request-map)
											 (not-home-parse request-map))]
		response-map))

(deftype TTTResponder []
	Responder
	(respond [this request]
		(create-response-map request)))

(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[clj-http.client :as client]
						[clojure.walk :as walk])
	(:import (server Responder)))

(def root (atom (str (.getCanonicalPath (io/file "./tictactoe")))))

(def file-map {:waiting     "/index.html" :restart? "/continue?.html" :user-setup "/user-setup.html" :player-setup "/player-setup.html" :level-setup "/level-setup.html"
							 :board-setup "/board-setup.html" :ready-to-play "/ttt.html" :playing "/ttt.html" :game-over "/game-over.html"})

;; COMPLETE TODO - GLM : File Rsponder already serves files--don't dulplicate, let him do that

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

(defn eat-cookies [request]
	(when (.contains (keys request) :Cookie)
		(walk/keywordize-keys (:Cookie request))))

(defn parse-request-for-game [request]
	(let [crude-resource (:resource request)
				split-resource (remove empty? (str/split crude-resource #"/"))]
		(when (> (count split-resource) 1)
			(let [target (rest split-resource)
						type (keyword (first (str/split (first target) #"\?")))
						request (assoc request :responder type :target target)]
				(assoc request :entry (extract-game-entry request) :cookie (eat-cookies request))))))

(defn home? [resource]
	(or (nil? resource) (= "/ttt" resource)))

(defn send-request-to-game [request]
	(let [game (manager/manage-game request)]
		(assoc request :resource (get file-map (:status game)) :cookie {:gameID (:gameID game) :users (:users game) :player1 (:player1 game) :player2 (:player2 game) :level (:level game) :board-size (:board-size game)})))

(defn prep-for-game [request]
	(if (home? (:resource request))
		(assoc request :resource "/index.html")
		(send-request-to-game (parse-request-for-game request))))

(defn set-new-request-for-reroute [request]
	(let [resource (:resource request)
				new-request-map {"re-route"    "true"
										 "method"      "GET"
										 "resource"    resource
										 "Host"        (get request :Host)
										 "httpVersion" "HTTP/1.1"}]
		(if (not (nil? (:cookie request)))
			(assoc new-request-map "cookie" (:cookie request))
			new-request-map)))

(defn create-response-map [request]
	(let [request-map (expand-java-map request)
				request-with-game (prep-for-game request-map)
		new-request (set-new-request-for-reroute request-with-game)]
		new-request))

(deftype TTTResponder []
	Responder
	(respond [this request]
		(create-response-map request)))

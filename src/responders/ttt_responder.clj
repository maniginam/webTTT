(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager]
						[clj-http.client :as client])
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

(defn eat-cookies [requestMap]
	(when (contains? requestMap :cookie)
		(let [cookie (str/split (:cookie requestMap) #"=")]
			{:state-id (first cookie) :gameID (last cookie)})))

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

(defn prep-for-game [request]
	(let [request-for-game (if (home? (:resource request))
													 request
													 (parse-request-for-game request))]
		(assoc request :game (manager/manage-game request-for-game))))

(defn set-new-request-for-reroute [request]
	(let [status (get (:game request) :status)
				cookie (if (= :playing status) "snickerdoodle" "oreo")
				file (str (get file-map status))
				new-request {"re-route"    "true"
										 "method"      "GET"
										 "resource"    file
										 "Host"        (get request :Host)
										 "httpVersion" "HTTP/1.1"
										 "cookie"      (str cookie "=" (get (:game request) :gameID))}]
		new-request))

(defn create-response-map [request]
	(let [request-map (expand-java-map request)
				request-with-game (prep-for-game request-map)]
		(set-new-request-for-reroute request-with-game)))

(deftype TTTResponder []
	Responder
	(respond [this request]
		(create-response-map request)))

(ns responders.ttt-responder
	(:require [clj-time.core :as time]
						[clojure.java.io :as io]
						[clojure.string :as str]
						[game.game-manager :as manager])
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
				]
		(last entries)))
;		entry-map (assoc {} key (last entries))]
;entry-map))

(defn eat-cookies [request]
	(when (.contains (keys request) :Cookie)
		(let [maybe-cookies (str/split (:Cookie request) #"; ")
					cookies (if (< 1 (count maybe-cookies)) (map #(clojure.edn/read-string %) maybe-cookies))
					]
			(let [after-nils (remove #(nil? %) cookies)
						max-gameID (if (zero? (count after-nils)) 0 (apply max (remove #(nil? %) (for [cookie cookies] (:gameID cookie)))))
						cookie-game (if (zero? (count after-nils)) nil (first (filter #(= max-gameID (:gameID %)) (remove #(nil? %) cookies))))]
				cookie-game))))

(defn parse-request-for-game [request]
	(let [crude-resource (:resource request)
				split-resource (remove empty? (str/split crude-resource #"/"))]
		(when (> (count split-resource) 1)
			(let [target (rest split-resource)
						type (keyword (first (str/split (first target) #"\?")))
						request (assoc request :responder type :target target)]
				(assoc request :entry (extract-game-entry request) :Cookie (eat-cookies request))))))

(defn home? [resource]
	(or (nil? resource) (= "/ttt" resource)))


(defn bake-cookie [game gameID]
	(println "(:player1 game): " (:player1 game))
	{:gameID  (if (nil? gameID) (:gameID game) gameID)
	 :status  (:status game)
	 :users   (:users game)
	 :player1 (:player1 game) :player2 (:player2 game)
	 :level   (:level game) :board-size (:board-size game)})

(defn send-request-to-game [request]
	(let [game (manager/manage-game request)]
		(println "(:status game): " (:status game))
		(assoc request :resource (get file-map (:status game))
									 :Cookie game
									 :game game)))

(defn prep-for-game [request]
	(if (home? (:resource request))
		(assoc request :resource "/index.html" :Cookie (bake-cookie {:status :waiting} 0))
		(send-request-to-game (parse-request-for-game request))))

(defn set-new-request-for-reroute [request]
	(let [resource (:resource request)
				cookie (:Cookie request)
				new-request-map {"statusCode"  (int 302)
												 "method"      "GET"
												 "Location"    resource
												 "resource"    resource
												 "Host"        (get request :Host)
												 "httpVersion" "HTTP/1.1"
												 "Set-Cookie"  (bake-cookie (:game request) (get (:game request) :gameID))}]
		new-request-map))

(defn create-response-map [request]
	(let [request-map (expand-java-map request)
				request-with-game (prep-for-game request-map)
				new-request (set-new-request-for-reroute request-with-game)]
		new-request))

(deftype TTTResponder []
	Responder
	(respond [this request]
		(create-response-map request)))

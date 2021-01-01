(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[responders.core :as rcore])
	(:import (server Responder)
					 (httpServer HttpResponseBuilder))
	)

(defn expand-java-map [request]
	(loop [keys (.keySet request)
				 key (first keys)
				 requestMap {}]
		(if (empty? keys)
			requestMap
			(let [requestMap (assoc requestMap (keyword key) (.get request key))]
				(recur (rest keys) (first (drop 1 keys)) requestMap)))))

(defn build-response-map [request]
	(let [crude-resource (:resource request)
				split-resource (remove empty? (str/split crude-resource #"/"))]
		(if (= 1 (count split-resource))
			(let [resource "/index.html"
						root (.getCanonicalPath (io/file (str "./" (:root request) resource)))
						body (slurp root)
						size (count body)
						response {"Server"         (:server-name request)
											"statusCode"     (int 200)
											"Content-Type"   "text/html"
											"body"           (.getBytes body)
											"Content-Length" size}]
				response)
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
				(.buildResponse builder (build-response-map (assoc requestMap :resource "/index.html" :root root :server-name (:server-name server-map))))
				(.buildResponse builder response)))))

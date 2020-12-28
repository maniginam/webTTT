(ns responders.ttt-responder
	(:require [clojure.java.io :as io])
	(:import (server Responder)
					 (httpServer HttpResponseBuilder))
	)

(defn expand-java-map [request keys]
	(loop [keys (.keySet request)
				 key (first keys)
				 requestMap {}]
		(if (empty? keys)
			requestMap
			(let [requestMap (assoc requestMap (keyword key) (.get request key))]
				(recur (rest keys) (first (drop 1 keys)) requestMap)))))

(deftype TTTRespond [root request]
	Responder
	(respond [this request]
		(let [builder (new HttpResponseBuilder)
					keys (.keySet request)
					requestMap (expand-java-map request keys)
					method (:method requestMap)
					mapResource (:resource requestMap)
					resource "/index.html"
					root (.getCanonicalPath (io/file (str "./" root resource)))
					body (slurp root)
					size (count body)
					response {"statusCode"     (int 200)
										"Content-Type"   "text/html"
										"body"           (.getBytes body)
										"Content-Length" size}]
			(.buildResponse builder response))))

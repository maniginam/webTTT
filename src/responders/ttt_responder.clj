(ns responders.ttt-responder
	(:require [clojure.java.io :as io]
						[responders.map-expander :as expander]
						[responders.core :as rcore]
						[responders.ttt-form-responder :as form-responder]
						[clojure.string :as str])
	(:import (server Responder)
					 (httpServer HttpResponseBuilder))
	)

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
			(let [target (rest split-resource)]
				(rcore/respond (assoc request :responder :form? :target target))))
		)
	)

(deftype TTTResponder [server-map ttt-map]
	Responder
	(respond [this request]
		(let [builder (new HttpResponseBuilder)
					root (:root server-map)
					requestMap (expander/expand-java-map request)
					mapResource (:resource requestMap)
					response (build-response-map (assoc requestMap :root root :server-name (:server-name server-map)))]
			(println "response: " response)
			(if (nil? response)
				(.buildResponse builder (build-response-map (assoc requestMap :resource "/index.html" :root root :server-name (:server-name server-map))))
				(.buildResponse builder response)))))

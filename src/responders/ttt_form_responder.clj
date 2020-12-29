(ns responders.ttt-form-responder
	(:require [clojure.java.io :as io]
						[responders.core :as rcore]
						[clojure.string :as str]))

(def players-map {:hvh 2 :hvc 1 :cvc 0})

(defmethod rcore/respond :form? [request]
	(let [target (str (first (:target request)))
				entries (str/split (second (str/split target #"\?")) #"&")
				entriesMap (map #(assoc {} (keyword (first %)) (second %)) (map #(str/split % #"=") entries))
				entryMap (into {} entriesMap)
				players-selection (first (filter #(= "on" (val %)) entryMap))
				players (if (empty? players-selection) nil ((key players-selection) players-map))
				response (if (nil? players)
									 nil
									 {"Server"       (:server-name request)
										"statusCode"   (int 200)
										"Content-Type" "text/html"
										;"body"           (.getBytes body)
										;"Content-Length" size})
										})]
		response))





(ns server.starter_spec
	(:require [speclj.core :refer :all]
						[server.starter :as starter]
						[clj-http.client :as client]
						[clojure.java.io :as io]
						[server.spec-helper :as helper])
	(:import (httpServer HttpConnectionFactory HttpResponseBuilder Server)))

(describe "Connection to Server"
	(before-all (starter/start-server 3141 "testroot"))
	(after-all (starter/stop))

	(it "responds with ttt index.html with blank request"
		(let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
					response (client/get "http://localhost:3141")]
			(should-contain target, (:body response))))

	(it "responds with ttt index.html with /ttt request"
		(let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
					response (client/get "http://localhost:3141/ttt")]
			(should-contain target, (:body response))))

	)

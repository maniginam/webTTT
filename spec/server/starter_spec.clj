(ns server.starter_spec
	(:require [speclj.core :refer :all]
						[server.starter :as starter]
						[clj-http.client :as client]
						[clojure.java.io :as io]
						[server.spec-helper :as helper])
	(:import (java.net Socket)
					 (java.io BufferedInputStream BufferedReader InputStreamReader)
					 (server SocketHost Router)
					 (httpServer HttpConnectionFactory HttpResponseBuilder Server)))

(describe "Connection to Server"
	(before-all (helper/connect 3141))
	(after-all (helper/stop))

	(context "responds with ttt home"
		(it "with blank request"
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
						response (client/get "http://localhost:3141")]
				(should-contain target, (:body response))))

	(it "sends /ttt request"
		(let [request "GET HTTP/1.1"])
		))


	(context "Connection to TicTacToe"
		(it "starts tictactoe"
			(let [request "GET /ttt HTTP/1.1"]
				)
			)
		)
	)

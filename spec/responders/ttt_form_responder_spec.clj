(ns responders.ttt-form-responder-spec
	(:require [speclj.core :refer :all]
						[server.starter :as starter]
						[clojure.java.io :as io]
						[clj-http.client :as client]))

(describe "Home Screen Form"
	(context "responds to form input:"
		(before-all (starter/start-server 3141 "testroot"))
		(after-all (starter/stop))

		(it "nil"
			(let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
						response (client/get "http://localhost:3141/ttt/form?hvh=off&hvc=off&cvc=off&bsize=0")]
				(should-contain starter/server-name (get (:headers response) "Server"))
				(should-contain target, (:body response))))
		))
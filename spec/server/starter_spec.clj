(ns server.starter_spec
	(:require [speclj.core :refer :all]
						[server.starter :as starter]
						[clj-http.client :as client]
						[clojure.java.io :as io]))

(xit (describe "Connection to Server"

			 (it "splits options"
				 (should= {:help "-h" :config "-x" :port 1518 :root "hello" :console "terminal"}
									(starter/split-options (into-array ["-h" "-x" "-p" "1518" "-r" "hello" "terminal"]))))

			 (context "responds with ttt index.html for"
				 (before-all (starter/start-server 1518 "testroot"))
				 (after-all (starter/stop) (Thread/sleep 1000))

				 (it "blank request"
					 (let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
								 response (client/get "http://localhost:1518")]
						 (should-contain starter/server-name (get (:headers response) "Server"))
						 (should-contain "Human VS Human" (:body response))
						 (should= target, (:body response))))

				 (it "/ request"
					 (let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
								 response (client/get "http://localhost:1518/")]
						 (should-contain "Human VS Computer" (:body response))
						 (should= target, (:body response))))

				 (it "/ttt request"
					 (let [target (slurp (.getCanonicalPath (io/file "./testroot/index.html")))
								 response (client/get "http://localhost:1518/ttt")]
						 (should-contain "Computer VS Computer" (:body response))
						 (should= target, (:body response))))
				 )
			 )
		 )
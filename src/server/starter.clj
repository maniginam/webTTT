(ns server.starter
	(:require [clojure.java.io :as io]
						[responders.ttt-responder :as ttt-responder])
	(:import (httpServer Server HttpConnectionFactory)
					 (server Router SocketHost)
					 (java.net Socket)))

(def server-name (Server/serverName))
(def server-atom (atom nil))
(def socket-atom (atom nil))

(defn register-responders [router root]
	(Server/registerResponders router (.getCanonicalPath (io/file (str "./" root))))
	(reset! ttt-responder/root root)
	(let [ttt-regex #"/ttt"
				ttt-responder (ttt-responder/->TTTResponder)]
		(.registerResponder router "GET" ttt-regex ttt-responder)))

;; TODO - GLM : why am i parsing options when server already does this; how to make reusable?!

(defn start-server [port root]
	(let [router (new Router)
				factory (new HttpConnectionFactory router)
				host (new SocketHost port factory)
				server {:host host :router router}]
		(reset! server-atom server)
		(register-responders router root)
		(.start host)))

(defn -main [& args]
	(let [options (into-array String ["-p" "1234" "-r" "tictactoe"])
				args-map (Server/makeArgMap options)]
		(Server/submitArgs options)
		(start-server (Integer/parseInt (get args-map "-p")) (get args-map "-r"))))


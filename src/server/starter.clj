(ns server.starter
	(:require [clojure.java.io :as io]
						[game.game-manager :as manager]
						[responders.ttt-responder :as ttt-responder])
	(:import (httpServer Server HttpConnectionFactory)
					 (server Router SocketHost)
					 (java.net Socket)))

(def server-atom (atom nil))
(def socket-atom (atom nil))
(def server-name (Server/serverName))
(def root (atom "tictactoe"))
(def port (atom 1234))
(def console (atom :gui))

(defn register-responders [router root]
	(Server/registerResponders router (.getCanonicalPath (io/file (str "./" root))))
	(let [server-map {:router router :root root :server-name server-name}
				ttt-regex #"/ttt"
				ttt-responder (ttt-responder/->TTTResponder server-map)]
		(.registerResponder router "GET" ttt-regex ttt-responder)))

(defn start-server [port root]
	(let [router (new Router)
				factory (new HttpConnectionFactory router)
				host (new SocketHost port factory)
				server {:host host :router router}]
		(register-responders router root)
		(reset! server-atom server)
		(.start host)
		(reset! socket-atom (new Socket "localhost" port))))

(defn stop []
	(assert @server-atom "no server running")
	(let [host (:host @server-atom)]
		(.end host))
	(.close @socket-atom)
	(reset! server-atom nil)
	(reset! socket-atom nil)
	(Thread/sleep 500))
;; TODO - GLM : why am i parsing options when server already does this; how to make reusable?!
(defn split-options [options]
	(loop [options options
				 opts {}
				 opt (first options)]
		(if (empty? options)
			opts
			(cond (= "-h" opt) (recur (rest options) (assoc opts :help "-h") (first (rest options)))
						(= "-x" opt) (recur (rest options) (assoc opts :config "-x") (first (rest options)))
						(= "-p" opt) (recur (rest options) (assoc opts :port (Integer/parseInt (second options))) (first (rest options)))
						(= "-r" opt) (recur (rest options) (assoc opts :root (second options)) (first (rest options)))
						(or (= "terminal" opt) (= "gui" opt)) (recur (rest options) (assoc opts :console opt) (first (rest options)))
						:else (recur (rest options) opts (first (rest options)))))))

(defn -main [& options]
	(let [opts (split-options options)]
		(println "Running on port " (:port opts))
		(println "Serving from: " (:root opts))
		(reset! root (:root opts))
		(reset! port (:port opts))
		(reset! console (keyword (:console opts)))
		(swap! manager/game assoc :console @console)
		(start-server @port @root)))


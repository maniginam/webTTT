(ns server.starter
	(:require [clojure.java.io :as io]
						[clojure.string :as str]
						[clojure.java.shell :as shell]
						[responders.ttt-responder :as ttt-responder]
						[responders.ttt-form-responder :as form-responder])
	(:import (httpServer Server HttpConnectionFactory)
					 (server Router SocketHost)
					 (java.net Socket)))

(def server-atom (atom nil))
(def socket-atom (atom nil))
(def server-name (Server/serverName))
(def root (atom "testroot"))
(def port (atom 1234))
(def console (atom :gui))

;(deftype Foo []
;  Responder
;  (respond [this request builder] "hello"))

(defn register-responders [router root]
	(Server/registerResponders router (.getCanonicalPath (io/file (str "./" root))))
	(let [server-map {:router router :root root :server-name server-name}
				ttt-map {:console @console}
				ttt-regex #"/ttt"
				;ttt-form-regex #"/ttt/form?"
				ttt-responder (ttt-responder/->TTTResponder server-map ttt-map)]
				;ttt-form-responder (form-responder/->TTTFormResponder root server-name)]
		(.registerResponder router "GET" ttt-regex ttt-responder)
		;(.registerResponder router "GET" ttt-form-regex ttt-responder)
		))

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
	(.stop (:host @server-atom))
	(.close @socket-atom)
	(reset! server-atom nil)
	(reset! socket-atom nil))

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
					(println "(:port opts): " (:port opts))
					(println "(:root opts): " (:root opts))
					(reset! root (:root opts))
					(reset! port (:port opts))
					(reset! console (:console (keyword opts)))
					(start-server @port @root)))


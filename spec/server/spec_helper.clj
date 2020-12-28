(ns server.spec-helper
	(:require [speclj.core :refer :all]
						[clojure.java.io :as io])
	(:import (httpServer HttpConnectionFactory HttpResponseBuilder Server)
					 (server SocketHost Router)
					 (java.net Socket)))

(def server-atom (atom nil))
(def socket-atom (atom nil))

(defn connect [port]
	(assert (nil? @server-atom) "server already running")
	(let [router (new Router)
				factory (new HttpConnectionFactory router)
				host ^:host (new SocketHost port factory)
				server {:host host :router router}]
		(Server/registerResponders router (.getCanonicalPath (io/file "./testroot")))
		(reset! server-atom server)
		(.start host)
		(reset! socket-atom (new Socket "localhost" port))))

(defn stop []
	(assert @server-atom "no server running")
	(.stop (:host @server-atom))
	(.close @socket-atom)
	(reset! server-atom nil)
	(reset! socket-atom nil))

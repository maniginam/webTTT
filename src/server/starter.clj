(ns server.starter
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [responders.ttt-responder :as ttt-responder])
  (:import (httpServer Server HttpResponseBuilder HttpConnectionFactory)
           (server Router SocketHost Responder)
           (java.net Socket)))

(def server-atom (atom nil))
(def socket-atom (atom nil))

;(deftype Foo []
;  Responder
;  (respond [this request builder] "hello"))

(defn start-server [port root]
  (let [router (new Router)
        factory (new HttpConnectionFactory router)
        host (new SocketHost port factory)
        server {:host host :router router}
        ttt-regex #"ttt"
        ttt-responder (ttt-responder/->TTTRespond root {:method "GET" :resource "/ttt" :root root :server-name Server/serverName})]
    (Server/registerResponders router (.getCanonicalPath (io/file (str "./" root))))
    (.registerResponder router "GET" ttt-regex ttt-responder)
    (reset! server-atom server)
    (.start host)
    ;(Thread/sleep 1000)
    (reset! socket-atom (new Socket "localhost" port))))

(defn stop []
  (assert @server-atom "no server running")
  (.stop (:host @server-atom))
  (.close @socket-atom)
  (reset! server-atom nil)
  (reset! socket-atom nil))

(defn start-ttt [config & options]
  (apply shell/sh (concat (str/split config #" ") options)))

(defn -main [& options]
  (let [serverOpts (remove #(or (= "terminal" %) (= "gui" %)) options)
        tttOpts (filter #(or (= "terminal" %) (= "gui" %)) options)]
  (start-server 1234 serverOpts)))


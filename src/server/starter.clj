(ns server.starter
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell])
  (:import (httpServer Server)))

(defn config []
  (let [file (str (.getCanonicalPath (io/file ".")) "/config.edn")
      config (read-string (slurp file))]
    config
  ))

(defn start-server [port root]
  (Server/main (into-array ["-p" "3141" "-r" "testroot"]))
  (let [serverMap (Server/getServerMap)]
    serverMap))
    ;(apply shell/sh (concat (str/split config #" ") options)))

(defn start-ttt [config & options]
  (apply shell/sh (concat (str/split config #" ") options)))

;(defn -main [& options]
;  (let [config (config)
;        serverOpts (remove #(or (= "terminal" %) (= "gui" %)) options)
;        tttOpts (filter #(or (= "terminal" %) (= "gui" %)) options)]
;  (start-server (:server config) serverOpts)
;  (start-ttt (:ttt config) tttOpts)))


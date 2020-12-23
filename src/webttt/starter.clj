(ns webttt.starter
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]))

(defn config []
  (let [file (str (.getCanonicalPath (io/file ".")) "/config.edn")
      config (read-string (slurp file))]
    config
  ))

(defn start-server [config & options]
    (apply shell/sh (concat (str/split config #" ") options)))

(defn start-ttt [config & options]
  (apply shell/sh (concat (str/split config #" ") options)))

(defn -main [& options]
  (let [config (config)]
  (start-server (:server config) options)
  (start-ttt (:ttt config) options)))

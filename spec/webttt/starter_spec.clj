(ns webttt.starter-spec
  (:require [speclj.core :refer :all]
            [webttt.starter :as starter]
            [webttt.spec-helper :as helper]
            [clojure.java.io :as io]))



(describe "Connection to Server"
  (context "HTTP Server"
    (it "starts server"
      (let [config (:server (starter/config))
            connection (starter/start-server config "-x" "-p" "3141" "-r" "testroot")
            output (:out connection)
            exit (:exit connection)]
        (should= 0 exit)
        (should-contain (str "Gina's Http Server") output)
        (should-contain (str "Running on port: 3141.") output)
        (should-contain (str "Serving files from: " (.getCanonicalPath (io/file "."))) output)))

    (it "sends a blank request"
      (let [request "GET HTTP/1.1"]
        ))
    )

  (context "Connection to TicTacToe"
    (it "starts tictactoe"
      (let [config (:ttt (starter/config))
            ttt (starter/start-ttt config "terminal")
            output (:out ttt)
            exit (:exit ttt)]
        (should= 0 exit)
        (should-contain (str "Welcome to Tic-Tac-Toe!") output)
        (should-contain (str "How many humans are playing?") output)
        )
      ))
  )

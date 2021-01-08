(ns html.core)

(def head (str "<html>\n<head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">\n</head>\n"))
(def body-start "<body>\n<h1>Tic Tac Toe</h1>\n")
(def svg-start "<svg width=\"100%\" height=\"100%\">\n")

(defmulti write! :status)

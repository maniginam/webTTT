(ns html.standard-lines)

(def head (str "<html>\n<head>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"/html/css/main.css\">\n</head>\n"))
(def body-start (str "<body>\n<h1>Tic Tac Toe</h1>\n"
										 "<svg width=\"100%\" height=\"100%\">\n"))

(defmulti write! :status)

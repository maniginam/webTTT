;(ns game.computer-request
;	(:require [clj-http.client :as client]
;						[game.game-manager :as manager]
;						[master.game-master :as game])
;	(:import (java.net Socket)))
;
;(defn make-request [game]
;	(let [port (:port game)
;				socket (new Socket "localhost" port)
;				updated-game (reset! manager/game (game/update-state game))
;				box (:box-played updated-game)
;				request (str "http://localhost:" port "/ttt/playing/box=" box)
;				]
;		)
;	)

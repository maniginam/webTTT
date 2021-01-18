(ns game.play-again
	(:require [game.game-manager :as manager]
						[spec-helper :as helper]
						[speclj.core :refer :all]))

(describe "Play Again Response"

	(context "play-again?"
			(it "yes please!"
				(let [request-for-play {:responder :play-again
																:Cookie (assoc helper/default-cookie :status :play-again)}
							game (manager/manage-game request-for-play)]
					(should= :user-setup (:status game))))
		)
	)
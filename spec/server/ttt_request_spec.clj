(ns server.ttt-request-spec
  (:require [speclj.core :refer :all]
						[server.spec-helper :as helper]))

;(describe "ttt request"
;	(before-all (helper/connect))
;	(after-all (helper/stop))
;	(context "ttt request"
;		(it "init response"
;			;(let [request {:method "GET" :resource "/ttt"}
;			;			response (responders.ttt-responder/respond request)])
;			)))
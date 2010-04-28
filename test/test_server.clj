;;; Tests of the server functionality
;
;(ns test-server
;    (:use [clojure.test :only (deftest, is, use-fixtures)])
;    (:use [clojure.contrib.server-socket])
;    (:use animate.core))
;    
;(def *animate-test-dir* "./animate-test")
;
;(defn server-fixture
;    [f]
;    (println "In server-fixture for " f)
;    (-main "--port" "6000")
;    (f))
; 
;(use-fixtures :once server-fixture)
;

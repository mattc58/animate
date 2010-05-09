;;; Tests of the server functionality
;
(ns test-server
   (:use [clojure.test :only (deftest, is, use-fixtures)])
   (:use [clojure.contrib.server-socket])
   (:use [clojure.contrib.io :only (reader, input-stream, read-lines)])
   (:use animate.core))
   
(def *animate-test-dir* "./animate-test")

(defn server-fixture
   [f]
   (-main "--port" "6000")
   (f))

(use-fixtures :once server-fixture)

(deftest test-server-alive
    (let [connection (input-stream "http://localhost:6000")]
        (is (= 16 (count (read-lines (reader connection)))))))

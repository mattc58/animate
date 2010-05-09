;;; Tests of the server functionality
;; NOTE: the /etc/hosts file must have entries for any URLs tested here
;
(ns test-server
    (:use [clojure.test :only (deftest, is, use-fixtures)])
    (:use [clojure.contrib.server-socket])
    (:use [clojure.contrib.io :only (reader, input-stream, read-lines)])
    (:import (java.net UnknownHostException))
    (:use animate.core))
   
(def *animate-test-dir* "./animate-test")

(defn server-fixture
    " this server fixture will be run once and will start the test server "
    [f]
    (-main "--port" "6000")
    (f)
    (close-server animate-server))

(use-fixtures :once server-fixture)

(deftest test-server-alive
    " test that the server is alive "
    (let [connection (input-stream "http://localhost:6000")]
        (is (< 0 (count (read-lines (reader connection)))))))
        
(deftest test-mc-alive
    " test that dev1.mattculbreth.com comes up "
    (let [connection (input-stream "http://dev1.mattculbreth.com:6000")]
        (is (< 0 (count (read-lines (reader connection)))))))

(deftest test-no-mc-alive
    " test that no dev3.mattculbreth.com comes up "
    (is (thrown? UnknownHostException (def connection (input-stream "http://dev3.mattculbreth.com:6000")))))
    
(deftest test-yi-alive
    " test that local.yieldidea.com comes up "
    (let [connection (input-stream "http://local.yieldidea.com:6000")]
        (is (< 0 (count (read-lines (reader connection)))))))

(deftest test-mc-content
    " test that the right content comes up for dev1.mattculbreth.com ")
    
(deftest test-yi-content
    " test that the right content comes up for local.yieldidea.com ")
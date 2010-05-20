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
    (def animate-server (-main "--port" "6000" "--config-dir" *animate-test-dir*))
    (f)
    (close-server animate-server))

(use-fixtures :once server-fixture)

(deftest test-server-alive
    " test that the server is alive "
    (let [connection (input-stream "http://localhost:6000")]
        (is (< 0 (count (read-lines (reader connection)))))))
        
(deftest test-ini-file
    " test that we can read an ini file for settings "
    (def server (-main "--ini-file" "settings_test.ini"))
    (let [connection (input-stream "http://localhost:9988")]
        (is (< 0 (count (read-lines (reader connection))))))
    (close-server server))
        
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
    " test that the right content comes up for dev1.mattculbreth.com "
    (let [  connection (input-stream "http://dev1.mattculbreth.com:6000")
            lines (read-lines (reader connection))]
        (is (some #{true} (map #(.contains % "<title>Matt Culbreth</title>") lines)))
        (is (some #{true} (map #(.contains % "<img src=\"/mattc_profile.jpg\">") lines)))
        (is (not (some #{true} (map #(.contains % "<title>Yield Idea</title>") lines))))))
        
(deftest test-mc-hierarchy
    " test that we can get a folder that's below the root "
    (let [  connection (input-stream "http://dev1.mattculbreth.com:6000/about/index.html")
            lines (read-lines (reader connection))]
        (is (some #{true} (map #(.contains % "<title>About Matt Culbreth</title>") lines)))
        (is (not (some #{true} (map #(.contains % "<title>Yield Idea</title>") lines))))))
                      
(deftest test-yi-content
    " test that the right content comes up for local.yieldidea.com "
    (let [  connection (input-stream "http://local.yieldidea.com:6000")
            lines (read-lines (reader connection))]
        (is (some #{true} (map #(.contains % "<title>Yield Idea</title>") lines)))
        (is (not (some #{true} (map #(.contains % "<title>Matt Culbreth</title>") lines))))))

;; Tests of the HTTP header functionality

(ns test-requests
    (:use [clojure.test :only (deftest, is)])
    (:use animate.core))
 
(deftest test-load-config-files
    " Test the load configs "
    (let [configs (load-config-files "./animate")]
        (is (not (nil? configs)))
        (is (> (count configs) 0))
        (is (= "mattculbreth.com" (:name (first configs))))))
        
(deftest test-make-http-request
    " test that we can turn the textual request coming in into a hashmap "
    (let [http-request (make-http-request ["GET /index.html HTTP/1.1" "Host: localhost" "Accept: */*" "User-Agent: Test"])]
        (is (not (nil? configs)))
        (is (= "GET" (:verb http-request)))
        (is (= "/index.html" (:resource http-request)))
        (is (= "localhost" (:host http-request)))))


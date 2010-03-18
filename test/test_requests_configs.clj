;; Tests of the HTTP requests and configs functionality

(ns test-requests
    (:use [clojure.test :only (deftest, is)])
    (:use animate.core))
    
(def *animate-test-dir* "./animate-test")
 
(deftest test-load-config-files
    " Test the load configs "
    (let [configs (load-config-files *animate-test-dir*)]
        (is configs)
        (is (> (count configs) 0))
        (is (= "mattculbreth.com" (:name (first configs))))))
        
(deftest test-make-http-request
    " test that we can turn the textual request coming in into a hashmap "
    (let [http-request (make-http-request ["GET /index.html HTTP/1.1" "Host: localhost" "Accept: */*" "User-Agent: Test"])]
        (is http-request)
        (is (= "GET" (:verb http-request)))
        (is (= "/index.html" (:resource http-request)))
        (is (= "localhost" (:host http-request)))))
        
(deftest test-find-config
    " test that we can find the configs after they've been loaded "
    (let [configs (load-config-files *animate-test-dir*)]
        (is configs)
        (is (= 1 (count (find-config "dev1.mattculbreth.com" configs))))
        (is (= 1 (count (find-config "dev2.mattculbreth.com" configs))))
        (is (= 1 (count (find-config "localhost" configs))))
        (is (= 1 (count (find-config "local.yieldidea.com" configs))))
        (is (= 0 (count (find-config "test does not exist" configs))))))


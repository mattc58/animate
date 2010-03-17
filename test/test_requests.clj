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


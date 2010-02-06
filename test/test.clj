(ns test
    (:use clojure.test)
    (:use animate))
 
;; just a simple test to show how the tests work    
(deftest test-matt
    (is (= 7 (matt))))

(ns test
    (:use clojure.test)
    (:use animate))
 
(defn anderson
    []
    (println "Anderson"))
    
(deftest test-anderson
    (is (= "anderson" "anderson")))
    
(deftest test-matt
    (is (= 7 (matt))))

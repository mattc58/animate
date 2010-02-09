(ns test
    (:use clojure.test)
    (:use animate))
 
(deftest test-css-header
    " Let's test the css header maker "
    (let [header (make-header 1024 "styles.css")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 1024"))
        (is (.contains header "Content-Type: text/css"))))

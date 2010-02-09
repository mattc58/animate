(ns test
    (:use clojure.test)
    (:use animate))
 
(deftest test-css-header
    " Let's test the css header maker "
    (let [header (make-header 1024 "styles.css")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 1024"))
        (is (.contains header "Content-Type: text/css"))))

(deftest test-html-header
    " Let's test the html header maker "
    (let [header (make-header 897 "animate.html")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 897"))
        (is (.contains header "Content-Type: text/html"))))

(deftest test-404-header
    " Let's test the 404 header maker "
    (let [header (make-header 250 nil)]
        (is (.contains header  "HTTP/1.1 404 Not Found"))
        (is (.contains header "Content-Length: 250"))
        (is (.contains header "Content-Type: text/html"))))

(deftest test-jpg-header
    " Let's test the jpg header maker "
    (let [header (make-header 145009 "mattc.jpg")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 145009"))
        (is (.contains header "Content-Type: image/jpeg"))))

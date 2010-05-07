;; Tests of the HTTP header functionality

(ns test-headers
    (:use [clojure.test :only (deftest, is)])
    (:require [animate.headers :as headers])
    (:use animate.core))
 
(deftest test-css-header
    " Let's test the css header maker "
    (let [header (headers/make-header 1024 "styles.css")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 1024"))
        (is (.contains header "Content-Type: text/css"))))

(deftest test-html-header
    " Let's test the html header maker "
    (let [header (headers/make-header 897 "animate.html")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 897"))
        (is (.contains header "Content-Type: text/html"))))

(deftest test-404-header
    " Let's test the 404 header maker "
    (let [header (headers/make-header 250 nil)]
        (is (.contains header  "HTTP/1.1 404 Not Found"))
        (is (.contains header "Content-Length: 250"))
        (is (.contains header "Content-Type: text/html"))))

(deftest test-jpg-header
    " Let's test the jpg header maker "
    (let [header (headers/make-header 145009 "mattc.jpg")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 145009"))
        (is (.contains header "Content-Type: image/jpeg"))))

(deftest test-png-header
    " Let's test the png header maker "
    (let [header (headers/make-header 145008 "mattc.png")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 145008"))
        (is (.contains header "Content-Type: image/png"))))

(deftest test-gif-header
    " Let's test the gif header maker "
    (let [header (headers/make-header 145007 "mattc.gif")]
        (is (.contains header  "HTTP/1.1 200 OK"))
        (is (.contains header "Content-Length: 145007"))
        (is (.contains header "Content-Type: image/gif"))))

(deftest test-500-header
    " Let's test the 500 header maker "
    (let [header (headers/make-header 998 "movie.mp4")]
        (is (.contains header "HTTP/1.1 500 Server Error"))
        (is (.contains header "Content-Length: 998"))
        (is (.contains header "Content-Type: text/html"))))

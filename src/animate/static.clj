;; Functions that deal with static file serving

(ns animate.static
    (:require [animate.headers :as headers])
    (:use [clojure.contrib.io :only (copy)])
    (:import (java.io File FilenameFilter FileNotFoundException BufferedReader OutputStream InputStreamReader OutputStreamWriter)))
 
(defn write-resource
    " write the resource "
    [out header resource file]
    (copy header out)
    (if resource
        (copy resource out)
        (copy file out)))

(defn serve-404
    " serve the 404 page for a site or the general one "
    [site-404-path stream config-dir]
    (try
        (let [notfound (if (nil? site-404-path) (slurp (str config-dir "/404.html")) (slurp site-404-path))]
            (write-resource stream (headers/make-header (.length notfound) nil) notfound nil))
    (catch FileNotFoundException e
        ;; can't find the 404 file (ironically), so try the general one
        (try
            (let [notfound (slurp (str config-dir "/404.html"))]
                (write-resource stream (headers/make-header (.length notfound) nil) notfound nil))
        (catch FileNotFoundException e
            ;; no site-wide 404, so just send a message
            (let [message "HTTP 404: Not Found\n"]
                (write-resource stream (headers/make-header (.length message) nil) message nil)))))))

(defn serve-resource
    " serve an actual resource (a file) "
    [host stream http-request resource-path config-dir]
    (println "Going to serve " resource-path " for " (first host))
    (if
         (empty? host)
         (serve-404 nil stream)
         (let [file-name (str (:files-root (first host)) resource-path)
             resource-file (File. file-name)]
             (if
                 (.exists resource-file)
                 (write-resource stream (headers/make-header (.length resource-file) file-name) nil resource-file)
                 (serve-404 (str (:files-root (first host)) "/404.html") stream config-dir)))))


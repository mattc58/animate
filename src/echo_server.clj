;; example code taken from 
;; http://stackoverflow.com/questions/1223352/writing-a-multiplexing-server-in-clojure/1223928#1223928 by alanlcode
(ns echo-server
  (:gen-class)
  (:use clojure.contrib.server-socket)
  (:import (java.io BufferedReader InputStreamReader OutputStreamWriter)))

;; I changed this to use a println instead of a print, which makes curl happier
(defn echo
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))
              *out* (OutputStreamWriter. out)]
      (loop []
        (let [input (read-line)]
          (println input)
          (flush))
        (recur))))
      
;; I changed this to use an explicit function instead of the inline anonymous one  
(defn echo-server []
  (println "In echo-server")
  (create-server 8080 echo))

;(def my-server (echo-server))

;; below written by me

(defn- writeln
    " utility function to write a string and a newline to a stream "
    [stream s]
    (doto stream
        (.write s)
        (.write "\n")
        (.flush)
    ))
    
(defn echo
    " a simple funciton to echo back to the client "
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))]
    (let [client-out (OutputStreamWriter. out)]
        (println "New client connection...")
        (loop []
            (let [input (read-line)]
                (if 
                    (> (.length input) 0)
                    (do
                        (writeln client-out input)
                        (recur)
                    )))))))
                    
(defn echo-batch
    " a batch-oriented echo that will get all lines from the client first "
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))]
    (let [client-out (OutputStreamWriter. out)]
        (println "New client connection...")
        (loop [lines []]
            (let [input (read-line)]
                (if 
                    (= (.length input) 0)
                    (doseq [line lines]
                        (writeln client-out line))
                (recur (cons input lines))))))))




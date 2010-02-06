;; example code taken from 
;; http://stackoverflow.com/questions/1223352/writing-a-multiplexing-server-in-clojure/1223928#1223928 by alanlcode
(ns echo-server
  (:gen-class)
  (:use clojure.contrib.server-socket)
  (:import (java.io BufferedReader InputStreamReader OutputStreamWriter)))

(defn echo
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))
              *out* (OutputStreamWriter. out)]
      (loop []
        (let [input (read-line)]
          (print input)
          (flush))
        (recur))))
        
(defn echo-server []
  ;(letfn [(echo [in out]
  ;                  (binding [*in* (BufferedReader. (InputStreamReader. in))
  ;                            *out* (OutputStreamWriter. out)]
  ;                    (loop []
  ;                      (let [input (read-line)]
  ;                        (print input)
  ;                        (flush))
  ;                      (recur))))]
  ;  (create-server 8080 echo)))
  (create-server 8080 echo))

(def my-server (echo-server))

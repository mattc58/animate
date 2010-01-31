(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line))
 
(defn -main [& args]
    ;; the main function, gets called on startup to process command line args
    (println "hello!")
    (with-command-line args
        "Animate: bringing Clojure web applications to life"
        [[port "The port to use" 5858]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "."]
         [tmp-dir "This is the tmp directory" "/tmp"]
     remaining]))
     
(defn matt
    []
    (+ 2 5))
     
  
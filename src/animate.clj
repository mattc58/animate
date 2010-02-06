(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line)
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

;; The main server process 
(defn run-server []
  (println "Listening...")
  (create-server 8080 echo))
  
(defn -main [& args]
    ;; the main function, gets called on startup to process command line args
    (with-command-line args
        "Animate: bringing Clojure web applications to life"
        [[port "The port to use" 5858]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "."]
         [data-dir "The directory to use for application data files" "."]
         [tmp-dir "This is the tmp directory" "/tmp"]
     remaining])
    (def animate-server (run-server)))
     
(defn matt
    []
    (+ 2 5))
    
    

  
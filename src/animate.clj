;; The main functions for Animate

(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use clojure.contrib.server-socket)
  (:import (java.io BufferedReader InputStreamReader OutputStreamWriter)))
 
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
        (loop [lines ()]
            (let [input (read-line)]
                (if 
                    (= (.length input) 0)
                    (doseq [line lines]
                        (writeln client-out line))
                (recur (cons input lines))))))))
    
                          
(defn run-server
    " The main server process "
    [port]
    (println "Listening to port" port "...")
    (create-server port echo-batch))
  
(defn -main [& args]
    "the main function, gets called on startup to process command line args"
    (with-command-line args
        "Animate: bringing Clojure web applications to life"
        [[port "The port to use" 5858]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "."]
         [data-dir "The directory to use for application data files" "."]
         [tmp-dir "This is the tmp directory" "/tmp"]
         remaining]
     (def animate-server (run-server port))))
     
(defn matt
    []
    (+ 2 5))
    
    

  
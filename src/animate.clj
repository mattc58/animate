(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use clojure.contrib.server-socket)
  (:import (java.io BufferedReader InputStreamReader OutputStreamWriter)))
 
;; Simple function to echo back to the client
(defn echo
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))]
        ;*out* (OutputStreamWriter. out)]
    (let [client-out (OutputStreamWriter. out)]
        (println "New client connection...")
        (loop []
            (let [input (read-line)]
                (if 
                    ; this seems like a HACK.  Thought input would be nil but it's not.
                    ; the important thing is that the function will exit if it's an
                    ; empty line from the client, and the server-socket library
                    ; will close the socket
                    (> (.length input) 0)
                    (do
                        (.write client-out input)
                        (.write client-out "\n")
                        (.flush client-out)
                        (recur)
                    )))))))
                          
;; The main server process 
(defn run-server
    [port]
    (println "Listening to port" port "...")
    (create-server port echo))
  
(defn -main [& args]
    ;; the main function, gets called on startup to process command line args
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
    
    

  
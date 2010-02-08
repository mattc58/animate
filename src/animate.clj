;; The main functions for Animate

(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use clojure.contrib.server-socket)
  (:use clojure.contrib.duck-streams)
  (:import (java.io File FileNotFoundException BufferedReader InputStreamReader OutputStreamWriter)))
 
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


(defn make-css-header
    " make a CSS header "
    [content-length]
    (list
        "HTTP/1.1 200 OK"
        "Content-Type: text/css"
        (str "Content-Length: " content-length) 
        "Server: Animate"
        "X-Powered-By: Animate"
        "\n"
        ))
        
(defn make-success-header
    " make the HTTP 200 header "
    [content-length]
    (list 
        "HTTP/1.1 200 OK"
        "Server: Animate"
        "X-Powered-By: Animate"
        "Content-Type: text/html; charset=utf-8"
        (str "Content-Length: " content-length) 
        "\n"))
    
(defn make-404-header
    " make the HTTP 404 not found header "
    [content-length]
    (list 
        "HTTP/1.1 404 Not Found"
        "Server: Animate"
        "X-Powered-By: Animate"
        "Content-Type: text/html; charset=utf-8"
        (str "Content-Length: " content-length) 
        "\n"))

(defn serve-resource
    " serve an actual resource (a file) "
    [stream verb resource-path]
    (let [file-name (str "." resource-path)
        resource-file (File. file-name)]
        (println "Going to serve" resource-file)
        (if
            (.exists resource-file)
            (let [resource (slurp file-name)]
                (doseq [line (concat 
                    (if
                        (.contains file-name ".css")
                        (make-css-header (.length resource))
                        (make-success-header (.length resource))
                    )  (list resource))]
                    (writeln stream line)))
            (let [notfound (slurp "404.html")]
                (doseq [line (concat (make-404-header (.length notfound)) (list notfound))]
                    (writeln stream line))))))

(defn handle-request
    " the function that handles the client request "
    [in out]
    (binding [*in* (BufferedReader. (InputStreamReader. in))]
    (let [client-out (OutputStreamWriter. out)]
        (println "New client connection...")
        (loop [lines []]
            (let [input (read-line)]
                (if 
                    (= (.length input) 0)
                    ;; 0 length line means it's time to serve the resource
                    (do
                        (let [last-line (seq (.split (last lines) " "))
                            verb (first last-line)
                            resource (nth last-line 1)]
                        ;; if it's only / then change it to /index.html
                        (serve-resource client-out verb (if (= resource "/") "/index.html" resource))))
                    ;; add to the lines vector and keep going
                    (recur (cons input lines))))))))
    
          
(defn run-server
    " The main server process "
    [port]
    (println "Listening to port" port "...")
    ;(create-server port echo-batch))
    (create-server port handle-request))
  
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
    
    

  
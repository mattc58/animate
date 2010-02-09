;; The main functions for Animate

(ns animate
  (:gen-class)
  (:use clojure.contrib.command-line)
  (:use clojure.contrib.server-socket)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.str-utils)
  (:import (java.io File FileNotFoundException BufferedReader InputStreamReader OutputStreamWriter)))
 
;; some globals for the server

;; a structure for configuration information
(defstruct config :name :files_root :url_root :server_names)
(def configs ())

;; the global configuration directory
(def config-dir "")

(defn make-css-header
    " make a CSS header "
    [content-length]
    (str-join "\n"
        (list
            "HTTP/1.1 200 OK"
            "Content-Type: text/css"
            (str "Content-Length: " content-length) 
            "Server: Animate"
            "X-Powered-By: Animate"
            "\n"
            )))
        
(defn make-image-header
    " make a CSS header "
    [content-length extension]
    (str-join "\n"
        (list
            "HTTP/1.1 200 OK"
            (str "Content-Type: image/" extension)
            (str "Content-Length: " content-length) 
            "Server: Animate"
            "X-Powered-By: Animate"
            "\n"
            )))

(defn make-success-header
    " make the HTTP 200 header "
    [content-length]
    (str-join "\n"
        (list 
            "HTTP/1.1 200 OK"
            "Server: Animate"
            "X-Powered-By: Animate"
            "Content-Type: text/html; charset=utf-8"
            (str "Content-Length: " content-length) 
        "\n")))
    
(defn make-404-header
    " make the HTTP 404 not found header "
    [content-length]
    (str-join "\n"
        (list 
            "HTTP/1.1 404 Not Found"
            "Server: Animate"
            "X-Powered-By: Animate"
            "Content-Type: text/html; charset=utf-8"
            (str "Content-Length: " content-length) 
            "\n")))

(defn write-resource
    " write a resourse with its header and then its content "
    [stream header content]
    (doto stream
        (.write header)
        (.flush)
        (.write content)
        (.flush)
    ))
    
(defn serve-resource
    " serve an actual resource (a file) "
    [stream verb resource-path]
    (let [file-name (str config-dir resource-path)
        resource-file (File. file-name)]
        (println "Going to serve" resource-file)
        (if
            (.exists resource-file)
            (let [resource (slurp file-name)]
                (write-resource
                    stream
                    (cond
                        (.contains file-name ".css") (make-css-header (.length resource))
                        (.contains file-name ".jpg") (make-image-header (.length resource) "jpeg")
                        :else (make-success-header (.length resource))
                    ) 
                    resource))
            (try
                (let [notfound (slurp (str config-dir "/404.html"))]
                    (write-resource stream (make-404-header (.length notfound)) notfound))
            (catch FileNotFoundException e
                ;; can't find the 404 file (ironically), so just send a message
                (let [message "HTTP 404: Not Found\n"]
                    (write-resource stream (make-404-header (.length message)) message)))))))

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
    [port config-dir tmp-dir]
    (println "Going to read config files at " config-dir)
    (def config-dir config-dir)
    (println "Listening to port" port "...")
    (create-server port handle-request))
  
(defn -main [& args]
    "the main function, gets called on startup to process command line args"
    (with-command-line args
        "Animate: bringing Clojure web applications to life"
        [[port "The port to use" 5858]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "./animate/mattculbreth.com"]
         [tmp-dir "This is the tmp directory" "./animate/tmp"]
         remaining] 
         (def animate-server (run-server port config-dir tmp-dir))))

  
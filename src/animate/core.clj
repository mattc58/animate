;; The main functions for Animate

(ns animate.core
    (:gen-class)
    (:use [clojure.contrib.command-line :only (with-command-line)])
    (:use [clojure.contrib.server-socket :only (create-server)])
    (:require [clojure.contrib.string :as ccs])
    (:import (java.io File FilenameFilter FileNotFoundException BufferedReader InputStreamReader OutputStreamWriter)))
 
;; some globals for the server

;; a structure for configuration information
(defstruct config-struct :name :files-root :url-root :host-names)
(def configs [])
(def req [])

;; a structure for HTTP requests
;(defstruct http-request-struct :verb :resource :protocol :user-agent :host :accept)

;; the global configuration directory
(def config-dir "")

(defn- make-css-header
    " make a CSS header "
    [content-length]
    (ccs/join "\n"
        [
            "HTTP/1.1 200 OK"
            "Content-Type: text/css"
            (str "Content-Length: " content-length) 
            "Server: Animate"
            "X-Powered-By: Animate"
            "\n"]
            ))
        
(defn- make-image-header
    " make a CSS header "
    [content-length file-name]
    (let [extension (.toLowerCase (.substring file-name (+ 1 (.lastIndexOf file-name ".")) (.length file-name)))]
        (join "\n"
            [
                "HTTP/1.1 200 OK"
                (str "Content-Type: image/" (if (= extension "jpg") "jpeg" extension))
                (str "Content-Length: " content-length) 
                "Server: Animate"
                "X-Powered-By: Animate"
                "\n"]
                )))

(defn- make-html-header
    " make the HTTP 200 header "
    [content-length]
    (ccs/join "\n"
        [ 
            "HTTP/1.1 200 OK"
            "Server: Animate"
            "X-Powered-By: Animate"
            "Content-Type: text/html; charset=utf-8"
            (str "Content-Length: " content-length) 
            "\n"]))
    
(defn- make-404-header
    " make the HTTP 404 not found header "
    [content-length]
    (ccs/join "\n"
        [ 
            "HTTP/1.1 404 Not Found"
            "Server: Animate"
            "X-Powered-By: Animate"
            "Content-Type: text/html; charset=utf-8"
            (str "Content-Length: " content-length) 
            "\n"]))

(defn- make-500-header
    " make the HTTP 500 server error header "
    [content-length]
    (ccs/join "\n"
        [ 
            "HTTP/1.1 500 Server Error"
            "Server: Animate"
            "X-Powered-By: Animate"
            "Content-Type: text/html; charset=utf-8"
            (str "Content-Length: " content-length) 
            "\n"]))
            
(defn make-header
    " generic function to make an HTTP header for a given type "
    [content-length file-name]
    (let [type (cond
        (nil? file-name) "404"
        (.contains file-name ".css") "css"
        ;; HACK: isn't there a contains-one-of type of function?
        (or 
            (.contains file-name ".jpg") 
            (.contains file-name ".gif") 
            (.contains file-name ".png")) "image"
        (.contains file-name ".html") "html"
        :else nil)]
    (cond
        (= type "css") (make-css-header content-length)
        (= type "image") (make-image-header content-length file-name)
        (= type "html") (make-html-header content-length)
        (= type "404") (make-404-header content-length)
        :else (make-500-header content-length)
    )))
    
(defn write-resource
    " write a resourse with its header and then its content "
    [stream header content]
    (doto stream
        (.write header)
        (.write content)
        (.flush)
    ))
    
(defn- find-config
    " find the config for a given host-name "
    [host-name]
    (for [item configs :while (not-empty (filter #(= % host-name) (:host-names item)))] item))
    
(defn serve-404
    " serve the 404 page for a site or the general one "
    [site-404-path stream]
    (println "404 trying to serve " site-404-path)
    (try
        (let [notfound (if (nil? site-404-path) (slurp (str config-dir "/404.html")) (slurp site-404-path))]
            (write-resource stream (make-header (.length notfound) nil) notfound))
    (catch FileNotFoundException e
        ;; can't find the 404 file (ironically), so try the general one
        (try
            (let [notfound (slurp (str config-dir "/404.html"))]
                (write-resource stream (make-header (.length notfound) nil) notfound))
        (catch FileNotFoundException e
            ;; no site-wide 404, so just send a message
            (let [message "HTTP 404: Not Found\n"]
                (write-resource stream (make-header (.length message) nil) message)))))))

(defn serve-resource
    " serve an actual resource (a file) "
    [stream http-request resource-path]
    (let [host (find-config (:host http-request))]
        (println "Host = " host)
        (if
            (empty? host)
            (serve-404 nil stream)
            (let [file-name (str (:files-root (first host)) resource-path)
                resource-file (File. file-name)]
                (println "Going to serve " resource-file)
                (if
                    (.exists resource-file)
                    (let [resource (slurp file-name)]
                        (write-resource stream (make-header (.length resource) file-name) resource))
                    (serve-404 (str (:files-root (first host)) "/404.html") stream))))))

(defn- make-http-request
    " make the http-request structure from the incoming request lines 
    :verb :resource :protocol :user-agent :host :accept
    The header comes in like:
    GET /index.html HTTP/1.
    <header key>: <header value>
    <header key>: <header value>
    and so on
    "
    [request-lines]
    (let [first-line (.split (first request-lines) " ") lines (filter not-empty (rest request-lines))]
        (def rl request-lines)
        ; go through each of the following header lines associng them to the map
        (merge (hash-map
            :verb (first first-line)
            :resource (second first-line)
            :protocol (nth first-line 2))
            (zipmap
                (map #(keyword (ccs/lower-case (.substring % 0 (.indexOf % ":")))) lines)
                (map #(.substring % (+ (.indexOf % ":") 2)) lines)))))

(defn handle-request
    " the function that handles the client request "
    [in out]
    (let [request (line-seq (BufferedReader. (InputStreamReader. in))) 
            http-request (make-http-request request)]
        (def req http-request)
        (serve-resource (OutputStreamWriter. out) http-request (if (= (:resource http-request) "/") 
            "/index.html" (:resource http-request)))))

(defn load-config-files
    " load the configuration files and put them in the configs vector "
    [config-dir]
    (def config-dir config-dir)
    (let [files (.list (File. config-dir) (proxy [FilenameFilter] [] (accept [dir name] (.endsWith name ".config"))))]
        (def configs (map #(merge (struct config-struct) (read-string (slurp (str config-dir "/" % )))) files))))
              
(defn run-server
    " The main server process "
    [port config-dir tmp-dir]
    (load-config-files config-dir)
    (println "Listening to port" port "...")
    (create-server port handle-request))
  
(defn -main [& args]
    "the main function, gets called on startup to process command line args"
    (with-command-line args
        "Animate: bringing Clojure web applications to life"
        [[port "The port to use" 5858]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "./animate"]
         [tmp-dir "This is the tmp directory" "./animate/tmp"]
         remaining] 
         (def animate-server (run-server port config-dir tmp-dir))))


;; The main functions for Animate

(ns animate.core
    (:gen-class)
    (:use [clojure.contrib.command-line :only (with-command-line)])
    (:use [clojure.contrib.io :only (read-lines, to-byte-array, copy)])
    (:use [clojure.contrib.server-socket :only (create-server)])
    (:use (clojure.contrib [string :only (join lower-case)]))
    (:import (java.io File FilenameFilter FileNotFoundException BufferedReader OutputStream InputStreamReader OutputStreamWriter)))
 
;; some globals for the server

;; a structure for configuration information
(defstruct config-struct :name :files-root :url-root :host-names)
(def *configs* [])

;; the global configuration directory
(def *config-dir* "")

(defn- make-css-header
    " make a CSS header "
    [content-length]
    (join "\n"
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
    (join "\n"
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
    (join "\n"
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
    (join "\n"
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
        :else (make-500-header content-length))))
    
(defn write-resource
    " write the resource "
    [out header content file]
    (copy header out)
    (copy file out))

(defn serve-404
    " serve the 404 page for a site or the general one "
    [site-404-path stream]
    (try
        (let [notfound (if (nil? site-404-path) (slurp (str *config-dir* "/404.html")) (slurp site-404-path))]
            (write-resource stream (make-header (.length notfound) nil) notfound))
    (catch FileNotFoundException e
        ;; can't find the 404 file (ironically), so try the general one
        (try
            (let [notfound (slurp (str *config-dir* "/404.html"))]
                (write-resource stream (make-header (.length notfound) nil) notfound))
        (catch FileNotFoundException e
            ;; no site-wide 404, so just send a message
            (let [message "HTTP 404: Not Found\n"]
                (write-resource stream (make-header (.length message) nil) message)))))))

(defn find-config
    " find the config for a given host-name "
    [host-name configs]
    (filter #(not (nil? %)) 
        (map (fn [item] (if (not-empty (filter #(.startsWith host-name %) (:host-names item))) item nil)) configs)))
        
(defn get-resource
    [file]
    (let [file-name (.getPath file)]
        (or 
            (.contains file-name ".jpg") 
            (.contains file-name ".gif") 
            (.contains file-name ".png"))
        (to-byte-array file)
        (slurp file-name)))

(defn serve-resource
    " serve an actual resource (a file) "
    [stream configs http-request resource-path]
    (let [host (find-config (:host http-request) configs)]
        (println "Going to serve " resource-path " for " (first host))
        (if
             (empty? host)
             (serve-404 nil stream)
             (let [file-name (str (:files-root (first host)) resource-path)
                 resource-file (File. file-name)]
                 (if
                     (.exists resource-file)
                     (let [resource (get-resource resource-file)]
                         (write-resource stream (make-header (count resource) file-name) resource resource-file))
                     (serve-404 (str (:files-root (first host)) "/404.html") stream))))))

(defn make-http-request
    " make the http-request structure from the incoming request lines 
    :verb :resource :protocol :user-agent :host :accept
    The header comes in like:
    GET /index.html HTTP/1.1
    <header key>: <header value>
    <header key>: <header value>
    and so on
    "
    [request-lines]
    (let [first-line (.split (first request-lines) " ") lines (take-while #(not-empty %) (rest request-lines))]
        (merge 
            (hash-map
                :verb (first first-line)
                :resource (second first-line)
                :protocol (nth first-line 2))
            (zipmap
                (map #(keyword (lower-case (.substring % 0 (.indexOf % ":")))) lines)
                (map #(.substring % (+ (.indexOf % ":") 2)) lines)))))

(defn handle-request
    " the function that handles the client request "
    [in out]
    (let [request (read-lines in)
            http-request (make-http-request request)]
        (serve-resource out *configs* http-request (if (= (:resource http-request) "/") 
            "/index.html" (:resource http-request)))))

(defn load-config-files
    " load the configuration files and put them in the configs vector "
    [config-dir]
    (let [files (.list (File. config-dir) (proxy [FilenameFilter] [] (accept [dir name] (.endsWith name ".config"))))]
        (map #(merge (struct config-struct) (read-string (slurp (str config-dir "/" % )))) files)))
              
(defn run-server
    " The main server process "
    [port config-dir tmp-dir]
    (def *config-dir* config-dir)
    (def *configs* (load-config-files *config-dir*))
    (create-server (Integer. port) handle-request))
  
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


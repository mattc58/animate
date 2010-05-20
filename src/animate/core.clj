;; The main functions for Animate
;; These functions will make calls out to static if it's a static file, or to applciations if 
;; the request belongs to an application

(ns animate.core
    (:gen-class)
    (:require [animate.headers :as headers])
    (:require [animate.static :as static])
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

(defn find-config
    " find the config for a given host-name "
    [host-name configs]
    (filter #(not (nil? %)) 
        (map (fn [item] (if (not-empty (filter #(.startsWith host-name %) (:host-names item))) item nil)) configs)))
        
(defn parse-http-request
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
            http-request (parse-http-request request)
            host (find-config (:host http-request) *configs*)]
        (static/serve-resource host out http-request (if (= (:resource http-request) "/") 
            "/index.html" (:resource http-request)) *config-dir*)))

(defn read-file-to-hashmap
    " load a file into a hashmap "
    [file]
    (read-string (slurp (str file))))

(defn load-config-files
    " load the configuration files and put them in the configs vector "
    [config-dir]
    (let [files (.list (File. config-dir) (proxy [FilenameFilter] [] (accept [dir name] (.endsWith name ".config"))))]
        (map #(merge (struct config-struct) (read-file-to-hashmap (str config-dir "/" % ))) files)))
              
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
        [[port "The port to use" 8080]
         [ip "This is the IP address to use" "127.0.1.1"]
         [config-dir "The directory to use for application config file" "./animate"]
         [tmp-dir "This is the tmp directory" "./animate/tmp"]
         [ini-file "The ini file in use" nil]
         remaining] 
         (let [settings (if ini-file (read-file-to-hashmap ini-file) {:port port :ip ip :config-dir config-dir :tmp-dir tmp-dir})] 
             (println "Animate loading with settings: " settings)
             (def animate-server (run-server (:port settings) (:config-dir settings) (:tmp-dir settings))))))


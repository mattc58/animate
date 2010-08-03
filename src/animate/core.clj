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
    (:import (java.io StringWriter File FilenameFilter FileNotFoundException BufferedReader OutputStream InputStreamReader OutputStreamWriter)))

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

(defn parse-cookies
    " grab the cookies out of the request and turn then into a hashmap "
    [http-request]
    (let [cookie (:cookie http-request)]
        (if cookie
            (let [split (map #(.split % "=") (.split cookie ";"))]
                (zipmap
                    (map (fn [pair] (first pair)) split)
                    (map (fn [pair] (second pair)) split))))))

(defn is-header?
    " returns true if this string is of a form key:value "
    [s]
    (> (.indexOf s ":") -1))

(defn is-form?
    " returns true if this string is of a form key=value "
    [s]
    (> (.indexOf s "=") -1))

(defn parse-headers
    [lines]
    (println "lines = \n" lines)
    (let [  headers (map (fn [pair] (.split pair ":")) (filter #(is-header? %) lines))
            forms (map (fn [pair] (.split pair "&")) (filter #(is-form? %) lines))]
        (merge
            (zipmap (map #(first %) headers) (map #(second %) headers))
            (let [pairs (map #(.split % "=") (first forms))]
                (zipmap (map #(first %) pairs) (map #(second %) pairs))))))


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
    (println "count = " (count request-lines))
    (println "0 = " (first request-lines))
    (println "1 = " (second request-lines))
    (println "2 = " (nth request-lines 2))
    (println "3 = " (nth request-lines 3))
    (doseq [l request-lines] (println l))
    (let [
        first-line (.split (.trim (first request-lines)) " ")
        lines (rest request-lines)
        request (merge
            (hash-map
               :verb (first first-line)
               :resource (second first-line)
               :protocol (nth first-line 2))
            (parse-headers lines))]
        (println "request:" (first first-line) (second first-line) (nth first-line 2))
        (dissoc (assoc request :cookies (parse-cookies request)) :cookie)))

(defn serve-login
    " serve the login form "
    [in host stream http-request config-dir]
    (if
        (empty? host)
        (static/serve-404 nil stream)
        (if (= "GET" (:verb http-request))
            (let [file-name (str config-dir "/login.html")
                resource-file (File. file-name)]
                (if
                    (.exists resource-file)
                    (static/write-resource stream (headers/make-header (.length resource-file) file-name) nil resource-file)
                    (static/serve-404 (str (:files-root (first host)) "/404.html") stream config-dir)))
            (static/serve-404 (str (:files-root (first host)) "/404.html") stream config-dir))))

(defn handle-request
    " the function that handles the client request "
    [in out]
    (do
        (def s (StringWriter. ))
        (copy in s)
    )
    (let [  request (.split (.toString s) "\n")
            http-request (parse-http-request request)
            host (find-config (:host http-request) *configs*)]
        (if (= (:resource http-request) "/login")
            (serve-login in host out http-request *config-dir*)
            (static/serve-resource host out http-request (if (= (:resource http-request) "/")
                "/index.html" (:resource http-request)) *config-dir*))))

(defn read-file-to-hashmap
    " load a file into a hashmap "
    [file]
    (read-string (slurp (str file))))

(defn load-config-files
    " load the configuration files and put them in the configs vector "
    [config-dir]
    (let [files (.list (File. config-dir) (proxy [FilenameFilter] [] (accept [dir name] (.endsWith name ".config"))))]
        (map #(merge (struct config-struct) (read-file-to-hashmap (str config-dir "/" % ))) files)))

(defn run-animate-inits
    " Run the animate-init functions on each of the dynamic apps "
    [configs]
    (doseq [app (filter #(= (:application-type %) :dynamic) configs)]
        (println "Running animate-init on " (:name app) "with namespace " (:application-namespace app))
        ;; LOAD or IMPORT or USE or REQUIRE the namespace (:application-namespace app).animate
        ;; Call animate-startup
        ;; Verify that resolve-url responds
        ;; Somehow load other namespaces? How will resolve-url work?
        ))

(defn run-server
    " The main server process "
    [port config-dir tmp-dir]
    (def *config-dir* config-dir)
    (def *configs* (load-config-files *config-dir*))
    (run-animate-inits *configs*)
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
             (run-server (:port settings) (:config-dir settings) (:tmp-dir settings)))))


;; The main functions for Animate

(ns animate.headers
    (:use (clojure.contrib [string :only (join lower-case)])))

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


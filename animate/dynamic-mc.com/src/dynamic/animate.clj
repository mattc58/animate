;; This is the code for an application.
;; Animate looks for two functions:
;;      aniamte-init: Called on Animate startup
;;      resolve-url: Called on each request to match on a url

(ns dynamic.animate)

(defn animate-init
    " This function is called by Animate when it first loads. Put any application init type stuff here."
    []
    (println "This is the animate-init for dynamic"))
    
(defn resolve-url
    " This function is called by Animate for each request. If a request URL should be served
      by the application, then return that function. Else, return nil. "
      [url]
      nil
      )

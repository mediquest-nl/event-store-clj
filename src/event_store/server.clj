(ns event-store.server
    (:require [org.httpkit.server :refer [run-server]]
              [clojure.pprint :refer [pprint]]
              [clojure.string :as str]
;              [clj-time.core :as t]
              [event-store.writer :as w])
    (:gen-class))

(defn to-html-page
  [s]
  (str "<html><head></head><body>" (str/replace s #"\n" "<br/>") "</body></html>"))

(defn ppr-str [x]
  (with-out-str (pprint x)))

(defn app [req]
;  (println "Received: " req)
  (w/write-event (pr-str req))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body "OK"})

(defn -main [& args]
  (let [server (run-server app {:port 8080})]
    (println "Server started on port 8080")
    (w/start-event-logger)
    (read-line)
    (server :timeout 100)
    (w/stop-event-logger))
  )

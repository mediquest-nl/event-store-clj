(ns event-store.writer
  (:require [clojure.java.io :as io]))

(println "COMPILING this")

(def ^:dynamic os nil)
(def nextId (atom 0))

(def flush-mode :openClose) ;; other options :immediate :none

(def event-file-name "events.bin")

(defn str-bytes-clj
  [s]
  (byte-array (map  byte s)))

(defn str-bytes-java
  [s]
  (.getBytes s))

(defn write-event
  [data]
  (let [the-os (if (= flush-mode :openClose)
                 (io/output-stream event-file-name :append true)
                 os)
        data  (if (string? data) (.getBytes data) data)]
    (.write the-os data 0 (count data))
    (.write the-os (int \newline))
  (swap! nextId inc)
  (case flush-mode
    :immediate (.flush the-os)
    :openClose (.close the-os)
    nil)))

(def catch-events true)

(defn start-event-logger
  []
  (println "Creating an event-logger with flush-mode=" flush-mode)
  (when (not= flush-mode :openClose) 
    (def os (io/output-stream event-file-name))))

(defn stop-event-logger
  []
  (when (not= flush-mode :openClose) ;; not necessary as os=nil
    (when os
      (.close os)
      (def os nil)))
  (println "Logged " @nextId " events."))

(defn test-write
  [n]
  (let [data (byte-array (map (partial + 65)  (range 50)))]
    (time (with-open [the-os (io/output-stream "tst.bin")]
            (binding [os the-os]
              (loop [n n]
                (when (>= n 0)
                  (write-event data)
                  (recur (dec n)))))))))

;(println "running a test")
;(w/test-write 1000000) 

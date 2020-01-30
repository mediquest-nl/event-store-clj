(ns event-store.test-client
  (:require [org.httpkit.client :as http]))


;;(def url "http://europe-west1-mediquest-sandbox.cloudfunctions.net/cvk-event-to-db-5")
(def url "http://localhost:8080")

(def options {:timeout 60000             ; ms  ;; default 200 ms
              :basic-auth ["user" "pass"]
              :query-params {:param "value" :param2 ["value1" "value2"]}
              :user-agent "User-Agent-string"
              :headers {"X-Header" "Value"}})

(defn get-now [] (java.time.LocalDateTime/now))

(defn- fire-burst
  [n]
  (let [success (atom 0)
        errors  (atom nil)]
    (loop [idx 0]
      (when (< idx n)
        (http/get url options
                  (fn [{:keys [status headers body error] :as resp}] ;; asynchronous response handling
                    (if error
                      (swap! errors (partial cons [idx resp]))
                      (swap! success inc))))
        (recur (inc idx))))        
  {:success success
   :errors  errors}))

(defn wait-burst
 [{:keys [success errors]} expect-num]
  (println "wait for " expect-num)
   (while (< (+ @success (count @errors)) expect-num)
     (Thread/sleep 10)))


(defn fire-bursts
  [num-burst requests-in-burst]
  (let [start-time (get-now)
        results (loop [curr-burst num-burst
                       q []]
                  (if (> curr-burst 0)
                    (let [burst-result (fire-burst requests-in-burst)]
                      (wait-burst burst-result requests-in-burst)
                      (recur (dec curr-burst) (conj q burst-result)))
                    q))]
   (let [bundle-red (fn [q {:keys [success errors]}]
                       [(+ (first q) @success) (apply conj (second q) @errors)]) 
         [success errors] (reduce bundle-red [0 []] results)
         _ (println "success = " success)
         _ (println "errors= " errors)
         returned-requests (+ success (count errors ))]
      (let [ready-time (get-now)]
      (println "StartTime: " start-time
               "ready " ready-time)
        (let [results 
    {:timing {:start-time    (.toString start-time)  
            :ready           (.toString ready-time)}
     :returned-requests-after-burst returned-requests
     :success success
     :errors  errors}]
 ;;      (spit "results.edn" (prn results))
       results
    )))))



(defn fire-tests
  [n]
  (let [success (atom 0)
        errors  (atom nil)
        start-time (get-now)] 
    (loop [idx 0]
      (when (< idx n)
        (http/get url options
                  (fn [{:keys [status headers body error] :as resp}] ;; asynchronous response handling
                    (if error
                      (swap! errors (partial cons [idx resp]))
                      (swap! success inc))))
        (recur (inc idx))))
    (let [requests-fired-time (get-now)
          returned-requests (+ @success (count @errors ))]
      (loop [num-returned returned-requests]
      (when (< num-returned n)
        (println " num-success: " @success " and num-errors=" (count @errors) " total " num-returned "  and num-fired=" n)
        (Thread/sleep 100)
        (recur (+ @success (count @errors)))))
      (let [ready-time (get-now)]
      (println "StartTime: " start-time
               "requests fired: " requests-fired-time "  intermediate num-request returned=" returned-requests
               "ready " ready-time)
        (let [results 
    {:timing {:start-time    (.toString start-time)  
            :request-fired   (.toString requests-fired-time)
            :ready           (.toString ready-time)}
     :returned-requests-after-burst returned-requests
     :success @success
     :errors  @errors}]
       (spit "results.edn" (prn results))
       results
    )))))



(ns rain.core
  (:require [clojure.spec.alpha :as s]
            [rain.water-level :refer [calculate-water-level]])
  (:gen-class))

(s/check-asserts true)

(s/def ::landscape (s/coll-of nat-int? :kind vector? :min-count 1))

(defn try-until-success
  "Executes callback until it's not throwing an error. Return first successful result"
  [cb]
  (loop []
    (let [result (try (cb)
                      (catch Exception e
                        (println "Your input is incorrect. Please try again")
                        false))]
      (if result
        result
        (recur)))))

(defn get-landscape
  "Gets landscape vector and validate it"
  []
  (println "Please enter landscape as a set of natural integers with comma as separator. Example: 1,3,4,6,2")
  (try-until-success #(s/assert ::landscape (read-string (str "[" (read-line) "]")))))

(defn get-hours []
  (println "Please enter the hours of rain. It should be positive integer")
  (try-until-success #(->> (read-line)
                           read-string
                           (s/assert nat-int?))))

(defn -main
  "Entry point"
  [& args]
  (let [landscape (get-landscape)
        hours (get-hours)]
    (println "Water level after rain: " (calculate-water-level landscape hours))))

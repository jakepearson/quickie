(ns leiningen.quickie
  (:require [leiningen.run :as run]
            [clojure.test :as t]
            [clojure.test.junit :as junit]
            [clojure.tools.namespace.repl :as r])
  (:import [org.joda.time DateTime]
           [org.joda.time.format DateTimeFormat]))

(defonce namespaces-to-test #"robotsanta\.test\..*")
(defonce date-format (DateTimeFormat/mediumDateTime))

(defn run-tests []
  (try
    (println (.print date-format (DateTime/now)))
    (t/run-all-tests #".*")
    (catch Exception e (println e))))

(defn clear-console []
  (println (str (char 27) "[2J")))

(defn reload []
  (r/refresh)
  (when *e
    (.printStackTrace *e)
    (set! *e nil)))

(defn reload-and-test []
  (reload)
  (clear-console)
  (run-tests))

(defn all-files [paths]
  (mapcat #(file-seq (clojure.java.io/file %)) paths))

(defn all-clj-files [paths]
  (filter #(.endsWith (.getName %) ".clj") (all-files paths)))

(defn get-file-state [paths]
  (reduce #(assoc %1 (.getAbsolutePath %2) (.lastModified %2)) {} (all-clj-files paths)))

(defn run-tests-forever [paths]
  (loop [current-state (get-file-state paths)
         changes current-state] (when (not (= changes current-state))
                                  (reload-and-test))
    (Thread/sleep 1000)
    (recur changes (get-file-state paths))))

(defn run-tests-once []
  (let [results (t/run-all-tests namespaces-to-test)
        failures (+ (:error results) (:fail results))]
    (when (> failures 0)
      (System/exit 1))))

(defn run [paths]
  (println "Fart")
  (try
    (reload)

    (do (run-tests) (run-tests-forever paths))

    (catch Exception e (println e))
    (finally
      (shutdown-agents))))

(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  ;(run/run project "-m" "leiningen.autotest" "-auto" (first (:source-paths project)) (first (:test-paths project))))
  (run [(first (:source-paths project)) (first (:test-paths project))]))
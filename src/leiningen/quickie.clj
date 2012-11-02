(ns leiningen.quickie
  (:require [leiningen.run :as run]
            [clojure.test :as t]
            [clojure.test.junit :as junit]
            [clojure.tools.namespace.repl :as r]))
 
(defn run-tests [project]
  (try
    (t/run-all-tests (:test-matcher project #".*test.*"))
    (catch Exception e (println e))))

(defn clear-console []
  (println (str (char 27) "[2J")))

(defn reload []
  (r/refresh)
  (when *e
    (.printStackTrace *e)
    (set! *e nil)))

(defn reload-and-test [project]
  (reload)
  (clear-console)
  (run-tests project))

(defn all-files [paths]
  (mapcat #(file-seq (clojure.java.io/file %)) paths))

(defn all-clj-files [paths]
  (filter #(.endsWith (.getName %) ".clj") (all-files paths)))

(defn get-file-state [paths]
  (reduce #(assoc %1 (.getAbsolutePath %2) (.lastModified %2)) {} (all-clj-files paths)))

(defn run-tests-forever [paths project]
  (loop [current-state (get-file-state paths)
         changes current-state] (when (not (= changes current-state))
                                  (reload-and-test project))
    (Thread/sleep 1000)
    (recur changes (get-file-state paths))))

(defn run [paths project]
  (try
    (reload)

    (do (run-tests project) (run-tests-forever paths project))

    (catch Exception e (println e))
    (finally
      (shutdown-agents))))

(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  ;(run/run project "-m" "leiningen.autotest" "-auto" (first (:source-paths project)) (first (:test-paths project))))
  (run [(first (:source-paths project)) (first (:test-paths project))] project))

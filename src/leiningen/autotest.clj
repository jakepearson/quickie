(ns leiningen.autotest
  (:require [clojure.test :as test]
            [clojure.tools.namespace.repl :as repl]))
 
(defn run-tests [project]
  (try
    (test/run-all-tests (:test-matcher project #".*test.*"))
    (catch Exception e 
      (do
        (println (.getMessage e))
        (.printStackTrace e)))))

(defn clear-console []
  (println (str (char 27) "[2J")))

(defn reload []
  (repl/refresh)
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
  (apply repl/set-refresh-dirs paths)
  (try
    (reload)

    (do (run-tests project) (run-tests-forever paths project))

    (catch Exception e (println e))
    (finally
      (shutdown-agents))))
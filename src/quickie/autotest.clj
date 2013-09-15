(ns quickie.autotest
  (:require [clojure.tools.namespace.repl :as repl]
            [quickie.runner :as runner]))
 
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
  (runner/run project))

(defn all-files [paths]
  (mapcat #(file-seq (clojure.java.io/file %)) paths))

(defn all-clj-files [paths]
  (filter #(.endsWith (.getName %) ".clj") (all-files paths)))

(defn get-file-state [paths]
  (reduce #(assoc %1 (.getAbsolutePath %2) (.lastModified %2)) {} (all-clj-files paths)))

(defn run-tests-forever [project]
  (loop [current-state (get-file-state (:paths project))
         changes       current-state]
    (when (not (= changes current-state))
      (reload-and-test project))
    (Thread/sleep 1000)
    (recur changes (get-file-state (:paths project)))))

(defn run [project]
  (apply repl/set-refresh-dirs (:paths project))
  (try
    (reload)

    (do
      (runner/run project)
      (run-tests-forever project))

    (catch Exception e (println e))
    (finally
      (shutdown-agents))))

(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]
            [leiningen.core.project :as lein-project]))

(defn paths [parameters project]
  (assoc parameters :paths (vec (concat (:source-paths project) (:test-paths project)))))

(defn default-pattern [project]
  (let [name (or (:name project)
                 (:group project))]
    (re-pattern (str name ".*test"))))

(defn test-matcher [project args]
  (cond
   (> (count args) 0)      (re-pattern (first args))
   (:test-matcher project) (:test-matcher project)
   :else                   (default-pattern project)))

(defn run-parallel [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.4.1"])
    (let [parameters (-> {}
                         (paths project)
                         (assoc :test-matcher (test-matcher project args)))]
      `(quickie.runner/run-parallel ~parameters))
    `(require 'quickie.runner)))

(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (let [project (-> project
                    (lein-project/merge-profiles [:test])
                    (update-in [:dependencies] conj ['quickie "0.4.1"]))
        parameters (-> {}
                       (paths project)
                       (assoc :test-matcher (test-matcher project args)))]
    (eval/eval-in-project
      project
      `(quickie.autotest/run ~parameters)
      `(require 'quickie.autotest))))

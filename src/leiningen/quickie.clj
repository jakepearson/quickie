(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]))

(defn paths [parameters project]
  (assoc parameters :paths [(first (:source-paths project)) (first (:test-paths project))]))

(defn test-matcher [parameters project]
  (if (:test-matcher project)
    (assoc parameters :test-matcher (:test-matcher project))
    (assoc parameters :test-matcher (re-pattern (str ".*" (:group project) ".*")))))
 
(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (println project)
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.1.0-SNAPSHOT"])
    (let [parameters (-> {}
                         (paths project)
                         (test-matcher project))]
      `(leiningen.autotest/run ~parameters))
    `(require 'leiningen.autotest)))

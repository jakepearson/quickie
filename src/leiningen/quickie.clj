(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]))

(defn paths [parameters project]
  (assoc parameters :paths [(first (:source-paths project)) (first (:test-paths project))]))

(defn default-pattern [project]
  (let [name (or (:name project)
                 (:group project))]
    (re-pattern (str ".*" name ".*"))))

(defn test-matcher [parameters project]
  (if-let [matcher (:test-matcher project)]
    (assoc parameters :test-matcher matcher)
    (assoc parameters :test-matcher (default-pattern project))))
 
(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.2.2"])
    (let [parameters (-> {}
                         (paths project)
                         (test-matcher project))]
      `(quickie.autotest/run ~parameters))
    `(require 'quickie.autotest)))

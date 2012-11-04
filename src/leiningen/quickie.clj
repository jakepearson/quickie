(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]))
 
(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.1.0-SNAPSHOT"])
    `(do
      (leiningen.autotest/run [~(first (:source-paths project)) ~(first (:test-paths project))] {:test-matcher ~(:test-matcher project)}))
    `(require 'leiningen.autotest)))

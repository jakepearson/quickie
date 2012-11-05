(ns leiningen.quickie
  (:require [leiningen.core.eval :as eval]))
 
(defn quickie
  "Automatically run tests when clj files change"
  [project & args]
  (eval/eval-in-project 
    (update-in project [:dependencies] conj ['quickie "0.1.0-SNAPSHOT"])
    (let [source-path (first (:source-paths project))
           test-path (first (:test-paths project))
           proj (if (:test-matcher project)
            {:test-matcher (:test-matcher project)}
            {})]
      `(leiningen.autotest/run [~source-path ~test-path] ~proj))
    `(require 'leiningen.autotest)))

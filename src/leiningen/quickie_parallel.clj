(ns leiningen.quickie-parallel
  (:require [leiningen.quickie :as quickie]))

(defn quickie-parallel [project & args]
  (apply quickie/run-parallel project args))


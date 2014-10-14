(ns leiningen.quickp
  (:require [leiningen.quickie :as quickie]))

(defn quickp
  "Run each test in a different thread"
  [project & args]
  (apply quickie/run-parallel project args))


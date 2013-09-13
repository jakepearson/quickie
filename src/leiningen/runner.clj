(ns leiningen.runner
  (:require [clojure.test :as test]
            [clansi.core :as clansi]
            [clojure.string :as string]))

(defn out-str-result [f]
  (let [string-writer (new java.io.StringWriter)]
     (binding [test/*test-out* string-writer]
       (let [result (f)]
         {:output (-> (str string-writer)
                      (string/split #"\n")
                      butlast)
          :result result}))))

(defn unneeded-line? [line]
  (let [line (string/trim line)]
    (or (re-seq #"^leiningen\.runner" line)
        (re-seq #"^leiningen\.autotest" line)
        (re-seq #"^user\$eval" line)
        (re-seq #"^clojure\.lang" line)
        (re-seq #"^clojure\.test" line)
        (re-seq #"^clojure\.core" line)
        (re-seq #"^clojure\.main" line)
        (re-seq #"^java\.lang" line)
        (re-seq #"^java\.util\.concurrent\.ThreadPoolExecutor\$Worker" line))))

(defn print-pass [result]
  (-> (:output result)
      last
      println)
  (println (clansi/style "   All Tests Passing!!!!   " :black :bg-green)))

(defn print-fail [result]
  (let [{:keys [error fail]} (:result result)
        lines               (:output result)]
    (->> lines
         (remove unneeded-line?)
         (string/join "\n")
         println)
    (println (clansi/style (str "   " error " errors and " fail " failures   ") :black :bg-red))))

(defn print-result [result]
  (let [{:keys [error fail]} (:result result)]
    (if (= 0 (+ error fail))
      (print-pass result)
      (print-fail result))))

(defn run [project]
  (try
    (let [matcher (:test-matcher project #"test")
          result  (out-str-result #(test/run-all-tests matcher))]
      (print-result result))
    (catch Exception e 
      (do
        (println (.getMessage e))
        (.printStackTrace e)))))


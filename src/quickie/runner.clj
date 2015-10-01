(ns quickie.runner
  (:require [clojure.test :as test]
            [clansi.core :as clansi]
            [com.climate.claypoole :as threadpool]

            [clojure.pprint]
            [clojure.tools.namespace.find :as find]
            [clojure.tools.namespace.track :as track]
            [clojure.tools.namespace.file :as file]
            [clojure.tools.namespace.repl :as repl]
            
            [clojure.string :as string]))


(defn- pad [length orig-str pad-char fu]
  (string/replace (format (fu length) orig-str) " " pad-char))

(defn- rpad
  ([length orig-str pad-char]
     (let [rfstr (fn [length] (str "%-" length "s"))]
       (pad length orig-str pad-char rfstr)))
  ([length orig-str]
     (rpad length orig-str ".")))

(defn- lpad
  ([length orig-str pad-char]
     (let [lfstr (fn [length] (str "%" length "s"))]
       (pad length orig-str pad-char lfstr)))
  ([length orig-str]
     (lpad length orig-str ".")))

(defn out-str-result [f]
  (let [string-writer (new java.io.StringWriter)]
     (binding [test/*test-out* string-writer]
       (let [result (f)]
         {:output (-> (str string-writer)
                      (string/split #"\n")
                      butlast)
          :result result}))))

(def matchers
  [#"^quickie\.runner"
   #"^quickie\.autotest"
   #"^user\$eval"
   #"^clojure\.lang"
   #"^clojure\.test"
   #"^clojure\.core"
   #"^clojure\.main"
   #"^java\.lang"
   #"^java\.util\.concurrent\.ThreadPoolExecutor\$Worker"
   #"^com\.climate\.claypoole"
   #"^java\.util\.concurrent"])

(defn needed-line [line]
  (let [line-trimmed (string/trim line)
        needed       (not-any? #(re-seq % line-trimmed) matchers)]
    {:line   line
     :needed needed}))

(def group-size 5)
(def prefix-size (long (/ group-size 2)))

(defn group-lines [lines]
  (let [matched-lines     (map needed-line lines)
        prefix            (repeat prefix-size nil)
        partionable-lines (concat prefix matched-lines prefix)]
    (partition group-size 1 partionable-lines)))

(defn filter-lines [lines]
  (let [groups       (group-lines lines)
        needed-line? (fn [group] (some :needed group))
        needed-lines (filter needed-line? groups)]
    (->> (map #(nth % prefix-size) needed-lines)
         (map :line))))

(defn print-pass [result]
  (-> (:output result)
      last
      println)
  (println (clansi/style "   All Tests Passing!   " :black :bg-green)))

(defn print-fail [result]
  (let [{:keys [error fail]} (:result result)
        lines                (:output result)]
    (->> lines
         filter-lines
         (string/join "\n")
         println)
    (println (clansi/style (str "   " error " errors and " fail " failures   ") :black :bg-red))))

(defn print-result [result]
  (let [{:keys [error fail]} (:result result)]
    (if (= 0 (+ error fail))
      (print-pass result)
      (print-fail result))))

(defn- duration-string [d]
  (str "(" (clansi/style (str d "ms") :magenta) ")"))

(defn print-ns-result [{:keys [success? ns duration output]} namespace-length]
  (let [result           (if success?
                           (clansi/style "Pass" :black :bg-green)
                           (clansi/style "Fail" :black :bg-red))
        rpad-length      (+ 20 namespace-length)
        lpad-length      18
        namespace-string (rpad rpad-length (ns-name ns))
        duration-string  (lpad lpad-length (duration-string duration))
        output-summary   (str namespace-string duration-string  " " result)]
    (println output-summary)
    (let [filtered-lines (->> (string/split output #"\n")
                              (drop 2)
                              filter-lines
                              (string/join "\n"))]
      (when-not (string/blank? filtered-lines)
        (println filtered-lines)))))

(defn- test-result [errors]
  {:pass     0
   :test     0
   :error    errors
   :fail     0
   :success? (= 0 errors)})

(defn capture-result-and-output [f]
  (let [output (java.io.StringWriter.)]
    (binding [*out*           output
              test/*test-out* output]
      [(f) (str output)])))

(defn- test-ns [ns]
  (let [start-time      (System/currentTimeMillis)
        [result output] (capture-result-and-output
                         (fn [] (try
                                  (test/test-ns ns)
                                  (catch Exception e
                                    (.printStackTrace e)
                                    (assoc (test-result 1) :exception e)))))]
    (merge result
           {:output   (str output)
            :success? (= 0 (+ (:error result) (:fail result)))
            :ns       ns
            :duration (- (System/currentTimeMillis) start-time)})))

(defn- summarize [test-results]
  (reduce (fn [results result]
            (-> results
                (assoc-in [:tests (str (ns-name (:ns result)))] result)
                (update-in [:summary :pass] + (:pass result))
                (update-in [:summary :test] + (:test result))
                (update-in [:summary :error] + (:error result))
                (update-in [:summary :fail] + (:fail result))))
          {:summary (test-result 0)}
          test-results))

(defn- longest-namespace-name-length [namespaces]
  (->> namespaces
       (map ns-name)
       (map str)
       (map count)
       (apply max 0)))

(defn- test-namespaces [matcher]
  (let [filter-fn (fn [ns] (let [ns-name (-> ns ns-name str)]
                             (re-find matcher ns-name)))]
    (->> (all-ns)
         (filter filter-fn))))

(defn run-parallel [project]
  (try
    (apply repl/set-refresh-dirs (:paths project ["./"]))
    (let [[refresh-result output] (capture-result-and-output repl/refresh)]
      (if (= :ok refresh-result)
        (let [test-nses         (test-namespaces (:test-matcher project))
              longest-ns-length (longest-namespace-name-length test-nses)
              lock              (Object.)
              start-time        (System/currentTimeMillis)
              results           (->> test-nses
                                     (threadpool/upmap 20
                                                       (fn [ns]
                                                         (let [result (test-ns ns)]
                                                           (locking lock
                                                             (print-ns-result result longest-ns-length))
                                                           result)))
                                     summarize
                                     doall)
              total-errors      (+ (get-in results [:summary :error])
                                   (get-in results [:summary :fail]))
              total-pass        (get-in results [:summary :pass])
              summary-string    (str "\nTests Passed: " total-pass " Tests Failed: " total-errors)
              duration          (duration-string (- (System/currentTimeMillis) start-time))
              background-color (if (= 0 total-errors) :bg-green :bg-red)]
          (println (clansi/style summary-string :black background-color) "  " duration)
          (System/exit total-errors)
          results)
        (do
          (println output)
          (throw refresh-result))))
    (catch Exception e
      (println e)
      (.printStackTrace e)
      (System/exit 1))))

(defn run [project]
    (try
    (let [matcher (:test-matcher project #"test")
          result  (out-str-result (partial test/run-all-tests matcher))]
      (print-result result))
    (catch Exception e 
      (println (.getMessage e))
      (.printStackTrace e))))


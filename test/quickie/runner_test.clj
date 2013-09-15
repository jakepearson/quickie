(ns quickie.runner-test
  (:require [clojure.test :refer :all]
            [quickie.runner :as runner]))

(deftest needed-line
  (are [expected input] (= expected (:needed (runner/needed-line input)))
       true  "a"
       true  ""
       false "clojure.lang"))

(deftest group-lines
  (is (= [[nil nil {:line "a" :needed true} nil nil]] (runner/group-lines ["a"]))))

(deftest filter-lines
  (testing "keep important lines"
    (is (= ["a" "b"] (runner/filter-lines ["a" "b"]))))
  (testing "should toss unimportant lines"
    (is (= [] (runner/filter-lines ["clojure.lang"]))))
  (testing "should keep lines near important lines"
    (let [lines ["clojure.lang" "a" "clojure.core" "clojure.core"]]
      (is (= lines (runner/filter-lines lines))))
    (let [lines ["a" "clojure.core" "clojure.core" "clojure.lang"]]
      (is (= 3 (count (runner/filter-lines lines)))))))


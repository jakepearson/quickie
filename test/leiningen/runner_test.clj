(ns runner-test
  (:require [clojure.test :refer :all]
            [leiningen.runner :as runner]))

(deftest out-str-result
  (let [f (fn []
            (print "foo")
            6)
        result (runner/out-str-result f)]
    (is (= "foo" (:string result)))
    (is (= 6 (:result result)))))

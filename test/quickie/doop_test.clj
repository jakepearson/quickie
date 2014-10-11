(ns quickie.doop-test
  (:require 
            [clojure.test :refer :all]))

(deftest stuff
  (is (= 1 3)))

(deftest throw
  (throw (Exception.)))

(defproject quickie "0.1.0-SNAPSHOT"
  :description "Automatically run tests when clj files change"
  :url "http://github.com/jakepearson/quickie"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/core.incubator "0.1.2"]
                 [midje "1.4.0"]
                 [org.clojure/tools.namespace "0.2.1"]])
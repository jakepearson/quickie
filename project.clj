(defproject quickie "0.1.0-SNAPSHOT"
  :description "Automatically run tests when clj files change"
  :url "http://github.com/jakepearson/quickie"
  :license {:name "Eclipse Public License"
  :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  ::test-paths ["test"]
  :test-matcher #".*test.*"
  :dependencies [
                 [org.clojure/core.incubator "0.1.2"]
                 [org.clojure/tools.namespace "0.2.1"]]
  :profiles {:dev {:dependencies [[joda-time/joda-time "2.1"]
                                  [org.clojure/clojure "1.3.0"]]}})
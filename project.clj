(defproject quickie "0.2.4"
  :description "Automatically run tests when clj files change"
  :url "http://github.com/jakepearson/quickie"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [midje "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [myguidingstar/clansi "1.3.0"]])

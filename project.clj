(defproject quickie "0.3.2"
  :description "Automatically run tests when clj files change"
  :url "http://github.com/jakepearson/quickie"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :signing {:gpg-key "37634C19"}
  :eval-in-leiningen true
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [midje "1.5.1"]
                 [org.clojure/tools.namespace "0.2.5"]
                 [com.climate/claypoole "0.3.1"]
                 [myguidingstar/clansi "1.3.0"]])

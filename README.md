# quickie

A Leiningen plugin to that will magically re-run all your tests when a file changes.

![Screenshot](doc/screen.png)

## Features

* Uses the builtin clojure.test test runner so you don't need to rewrite your tests
* Tools.namespace will unload and reload namespaces as needed to keep process in sync
* Runs every time a clojure file in your project changes
* Uses (Clansi)[https://github.com/ams-clj/clansi] to show a red or green bar to know if you tests are passing
* Filters out exception stacktraces to remove cruft

## Usage

Use this for project-level plugins:

Put `[quickie "0.2.1"]` into the `:plugins` vector of your project.clj.

    $ lein quickie

By default all namespaces in your classpath with that contain your project name and end with the work `test` will be tested for each run.  To change this, add a line like this to your project.clj `:test-matcher #"my regular expression"`

Hit ctrl+c whenever you are done.  Have fun!

## License

Copyright © 2012 Jake Pearson

Distributed under the Eclipse Public License, the same as Clojure.

## Contributors
* Adam Esterline
* Jeff Smith
* Russ Teabeault
* Chris Perkins
* Jake Pearson

# quickie

A Leiningen plugin to that will magically re-run all your tests when a file changes
## Usage

Use this for user-level plugins:

Put `[quickie "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your
`:user` profile, or if you are on Leiningen 1.x do `lein plugin install
quickie 0.1.0-SNAPSHOT`.

Use this for project-level plugins:

Put `[quickie "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

    $ lein quickie

Hit ctrl+c whenever you are done.  Have fun!

## License

Copyright © 2012 Jake Pearson

Distributed under the Eclipse Public License, the same as Clojure.

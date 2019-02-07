# debug-utils

A lightweight set of debug utilities. Very much in alpha! API is
liable to change.

`debug-utils` takes the point of view that configuration should be
expressed in environment variables. Currently, two environment variables are of interest:

 - `DEBUG` - when set (to anything!), `debug` blocks are inserted into
   code. Otherwise they are replaced by null.
 - `LOG_LEVEL` - may be "DEBUG" < "LOG" < "WARN" < "ERROR" < "SILENT".
   Defaults to "LOG". All logs have a level attached to them, and are
   only emitted as code when their level is greated than or equal to
   the `LOG_LEVEL`.

## Using the library

Just add this line to `:deps` in your `deps.edn`:

```cljs
github-idokutela/to-go {:git/url "https://github.com/idokutela/dev_utils"
                        :sha     "17dc775ca66a242b87ec0ffc0061f03bebe24f3d"}
```

then follow the example.

## API

Currently, there are two public macros:

 - `dev & <exprs>`: places the expressions followed by `nil`in a `do`
   block if the `DEBUG` variable is set, otherwise replaces with
   `nil`.

 - `log <level> & <message>`: Logs a message. Evaluates to nil.  The
   code is only emitted if the `LOG_LEVEL` is less than or equal to
   the given level.

## Example

```clojure
(ns bla
  (:require [dev-utils :as utils]))

;;; This will only print if the DEBUG environment is set.
(utils/dev
  (println "I'm in dev mode"))

;;; LOG-LEVEL defaults to "debug"
(utils/log :debug "This will only print when LOG-LEVEL = debug")
(utils/log :log "This will print when LOG-LEVEL=debug or LOG-LEVEL=log")
(utils/log :warn "This will print unless LOG-LEVEL=error or LOG-LEVEL=silent")
(utils/log :error "This will print unless LOG-LEVEL=silent.")
```


## License
MIT

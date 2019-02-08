# dev-utils

A lightweight set of debug utilities. Very much in alpha! API is
liable to change.

`dev-utils` takes the point of view that configuration should be
expressed in environment variables. Currently, two environment
variables are of interest:

 - `DEV` - when set (to anything!), `dev` blocks are inserted into
   code. Otherwise they are replaced by null.
 - `LOG_LEVEL` - may be "DEBUG" < "LOG" < "WARN" < "ERROR" < "SILENT".
   Defaults to "LOG". All logs have a level attached to them, and are
   only emitted as code when their level is greated than or equal to
   the `LOG_LEVEL`.

## Using the library

Just add this line to `:deps` in your `deps.edn`:

```cljs
github-idokutela/to-go {:git/url "https://github.com/idokutela/dev-utils"
                        :sha     "2681f431254f36df3a690f246bbe5148ad59babb"}
```

then follow the example.

## Example

```clojure
(ns bla
  (:require [dev-utils :as utils]))

;;; emit-<xxx>

;;; The emit-<xxx> family of macros emit code condiitiona on
;;; the value of an environment variable.
;;;
;;; Only the relevant code is emitted.

(utils/emit-if "FOO"
  (println "FOO is set.")
  (println "FOO is not set."))

(utils/emit-when "FOO"
  (println "emit-when emits a do block or nil.")
  (println "If FOO is set, the following code is emitted:")
  (println "(do
              (println \"emit-when emits a do block or nil.\")
              ...etc
			  nil)")
  (println "Otherwise nil is emitted."))

(utils/emit-when-not "FOO"
  (println "The complement of emit-when:")
  (println "Emits code when FOO is *not* set."))

;; One of the cases is emitted, depending on the value of FOO.
;; Emit-case is case-insensitive.
;; The default case is optional: if not present, is nil.
(utils/emit-case "FOO"
  "Hello" (println "FOO is set to HELLO")
  "Bar"   (println "FOO is set to Bar")
  (println "Neither of those cases applied."))


;; emit-case-sensitive is like emit-case, but matching is case-sensitive
(utils/emit-case-sensitive "FOO"
  "Hello" (println "FOO is set to HELLO")
  "Bar"   (println "FOO is set to Bar")
  (println "Neither of those cases applied."))


;;; dev is an alias to (emit-when "DEV" ...)
;;; in other words, the code will be emitted if and only if "DEV" is set.
(utils/dev (println "I'm in dev mode"))

;;; Logging

;;; The LOG_LEVEL environment variable controls the logging level.
;;; It defaults to "DEBUG".

;;; The log function logs depending on LOG_LEVEL.
(utils/log :debug "This will only print when LOG-LEVEL = debug")
(utils/log :log "This will print when LOG-LEVEL=debug or LOG-LEVEL=log")
(utils/log :warn "This will print unless LOG-LEVEL=error or LOG-LEVEL=silent")
(utils/log :error "This will print unless LOG-LEVEL=silent.")


;;; Miscellaneous

;;; The following macros evaluate to boolean constants.
(is-set? "FOO") ; true if "FOO" is set
(is-dev?)       ; alias for (is-set? "DEV")

;;; In clojure, one can use
(get-environment-variable "FOO") ; The value the FOO environment variable is set to.
```

## API

See the clojure docs.

## License
MIT

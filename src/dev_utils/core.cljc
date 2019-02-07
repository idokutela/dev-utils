(ns dev-utils.core
  #? (:cljs (:require-macros [dev-utils.core :refer [dev log]]))
  #? (:clj (:require [clojure.string :refer [lower-case]]
                     [clojure.pprint :refer [pprint]])
      :cljs (:require [cljs.pprint :refer [pprint]])))

;;; Code emission
(def DEBUG "DEBUG")


#?(:clj (defn get-environment-variable
          "Gets the environment variable `name`, optionally returning `default`
  if not set."
          [name & {:keys [default]}]
          (or (System/getenv name) default)))

#?(:clj (defn is-debug?
          []
          (some? (get-environment-variable DEBUG))))



(defmacro dev
  "Emits the expressions followed by `nil` in a `do` block when the DEBUG environment variable is set, otherwise removes them."
  [& exprs]
  (when (is-debug?) `(do ~@exprs nil)))



;;; Logging

(def LOG-LEVEL "LOG_LEVEL")
(def ^:private log-level-value
  {:debug  1
   :log    2
   :warn   3
   :error  4
   :silent 5})

#?(:clj (defn- log-level
          []
          (let [level (get-environment-variable LOG-LEVEL :default "LOG")]
            (keyword (lower-case level)))))
#?(:clj
   (defn- should-log?
     [level]
     (<= (log-level-value (log-level)) (log-level-value level))))



(defprotocol ILogger
  "A logger implements this protocol."
  (-warn  [logger message])
  (-log   [logger message])
  (-error [logger message])
  (-debug [logger message]))

(defn- pprint-items
  [items]
  (let [pprint-item (fn [item]
                      (if (string? item)
                        item
                        (clojure.string/trim
                         (with-out-str (pprint item)))))]
    (transduce
     (comp
      (map pprint-item)
      (interpose " "))
     str
     items)))

(def default-logger
  #?(:cljs (reify ILogger
             (-warn  [_ message] (.warn js/console (pprint-items message)))
             (-log   [_ message] (.log js/console (pprint-items message)))
             (-error [_ message] (.error js/console (pprint-items message)))
             (-debug [_ message] (.debug js/console (pprint-items message))))
     :clj (reify ILogger
            (-warn [_ message] (apply println "WARNING: " message))
            (-log [_ message]  (apply println "LOG: " message))
            (-error [_ message] (apply println "ERROR: " message))
            (-debug [_ message] (apply println "DEBUG: " message)))))



(defmacro log
  "Logs a message (and an optional s-exp) at the given level. Returns nil.

  The following levels are accepted: `:warn`, `:log`, `:error` and `:debug`.

  If the LOG_LEVEL variable is set to a level higher than the log level, the
  logging emits no code."
  [level & message]
  (let [message (into [] message)]
    (when (should-log? level)
      (case level
        :error `(-error default-logger ~message)
        :warn  `(-warn default-logger ~message)
        :log   `(-log default-logger ~message)
        :debug `(-debug default-logger ~message)))))

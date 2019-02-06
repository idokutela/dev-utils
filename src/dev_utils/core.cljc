(ns dev-utils.core
  #? (:cljs (:require-macros [dev-utils.core :refer [debug log]]))
  #? (:clj (:require [clojure.string :refer [lower-case]]
                     [clojure.pprint :refer [pprint]])
      :cljs (:require [cljs.pprint :refer [pprint]])))

#?(:clj (defn get-environment-variable
          [name & {:keys [default]}]
          (or (System/getenv name) default)))

(def DEBUG "DEBUG")

#?(:clj (defn is-debug?
          []
          (some? (get-environment-variable DEBUG))))

(defmacro debug
  "Inserts the expressions in a `do` block when the debug environment variable is set, otherwise removes them.

  The result is always `nil`."
  [& exprs]
  (when (is-debug?)
    '(do ~@exprs nil)))


(def LOG-LEVEL "LOG_LEVEL")

(defn- order-level
  [level]
  (case level
    :debug  1
    :log    2
    :warn   3
    :error  4
    :silent 5
    10000))

#?(:clj (defn get-log-level
          []
          (let [level (get-environment-variable LOG-LEVEL :default "LOG")]
            (keyword (lower-case level)))))

#?(:clj
   (defn- should-log?
     [level]
     (let [set-level (get-log-level)]
       (<= (order-level set-level) (order-level level)))))


(defprotocol ILogger
  "A logger implements this protocol.

  By default, it uses console in js, and println in java."
  (-warn  [logger message])
  (-log   [logger message])
  (-error [logger message])
  (-debug [logger message]))


;;; Soon, the logger will be configurable.

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
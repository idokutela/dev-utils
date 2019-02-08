(ns dev-utils.core
  #? (:cljs (:require-macros [dev-utils.core :refer [dev log]]))
  #? (:clj (:require [clojure.string :refer [lower-case]]
                     [clojure.pprint :refer [pprint]])
      :cljs (:require [cljs.pprint :refer [pprint]])))

;;; Code emission

#?(:clj (defn get-environment-variable
          "Gets the environment variable `name`, optionally returning `default`
  if not set."
          [name & {:keys [default]}]
          (or (System/getenv name) default)))


(defmacro is-set?
  "Evaluates to true if `env-var` is set."
  [env-var]
  (when-not (string? env-var)
    (throw (ex-info "env-var must be an actual string." nil)))
  (some? (get-environment-variable env-var)))


(defmacro is-dev?
  "Evaluates to `true` if DEV is set."
  []
  `(is-set? "DEV"))



(defmacro emit-case-sensitive
  "Emits code depending on the value of an environment variable.

  The syntax is similar to a case statement, except it defaults to
  `nil`.

  By default, the matching is *case-sensitive*. `emit-case` is
  analogous to `emit-case-sensitive`, but matches regardless of case.

  Note, the name of the env-var and all matches must be strings (and
  not things that evaluate to strings).

  Example:

      (emit-case-sensitive \"ENV_VAR\"
          \"BEEP\" (println \"ENV_VAR=BEEP\")
          \"BOOP\" (println \"ENV_VAR=BOOP\")
          (println \"ENV_VAR is some other value\"))"
  [env-var & cases]
  (when-not (string? env-var)
    (throw (ex-info "The environment variable name must be a string" nil)))
  (let [env-val (get-environment-variable env-var)
        cases (partition-all 2 cases)]
    (doseq [case cases]
      (when (and (= 2 (count case))
                 (-> case first string? not))
        (throw (ex-info "The case match must be a string" nil))))
    (when-some [env-val env-val]
      (reduce (fn [res [match expr :as case]]
                (cond
                  (= 1 (count case)) (reduced match)
                  (= env-val match) (reduced expr)
                  :else nil))
              nil
              cases))))


(defmacro emit-case
  "Emits code depending on the value of an environment variable.

  The syntax is similar to a case statement, except it defaults to
  `nil`.

  The matching is case-insensitive. `emit-case-sensitive`
  is analogous to `emit-case`, but is case sensitive.

  Note, the name of the env-var and all matches must be strings (and
  not things that evaluate to strings).

  Example:

      (emit-case \"ENV_VAR\"
          \"BEEP\" (println \"ENV_VAR=BEEP\")
          \"BOOP\" (println \"ENV_VAR=BOOP\")
          (println \"ENV_VAR is some other value\"))"
  [env-var & cases]
  (when-not (string? env-var)
    (throw (ex-info "The environment variable name must be a string" nil)))
  (let [env-val (when-some [env-val (get-environment-variable env-var)] (lower-case env-val))
        cases (partition-all 2 cases)]
    (doseq [case cases]
      (when (and (= 2 (count case))
                 (-> case first string? not))
        (throw (ex-info "The case match must be a string" nil))))
    (when-some [env-val env-val]
      (reduce (fn [res [match expr :as case]]
                (cond
                  (= 1 (count case)) (reduced match)
                  (= env-val (lower-case match)) (reduced expr)
                  :else nil))
              nil
              cases))))


(defmacro emit-if
  "Emits `then` if `env-var` is set to anything, otheriwise `then`."
  [env-var then else]
  (if (some? (get-environment-variable env-var))
    then
    else))


(defmacro emit-when
  "Emits the expressions in a do block followed by nil when the `env-var` is set."
  [env-var & exprs]
  (when (some? (get-environment-variable env-var))
    `(do ~@exprs nil)))


(defmacro emit-when-not
  "Emits the expressions in a do block followed by nil when the `env-var` is not set."
  [env-var & exprs]
  (when (nil? (get-environment-variable env-var))
    `(do ~@exprs nil)))


(defmacro dev
  "Emits the expressions followed by `nil` in a `do` block when the DEV environment variable is set, otherwise removes them."
  [& exprs]
  `(emit-when "DEV" ~@exprs))



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

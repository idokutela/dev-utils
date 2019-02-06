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

## API

Currently, there are two public macros:

 - `debug`: places a code block in a `do` block if the `DEBUG`
   variable is set, otherwise replaces with `nil`. The `do` block
   _always_ evaluates to `nil`.

 - `log <level> & <message>`: Logs a message. Evaluates to nil.  The
   code is only emitted if the `LOG_LEVEL` is less than or equal to
   the given level.

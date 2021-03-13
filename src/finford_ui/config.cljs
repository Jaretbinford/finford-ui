(ns finford-ui.config)

;; DEBUG is special. It is always defined but we override it
(def debug? ^boolean goog.DEBUG)

;; All other `goog-define` use the following syntax:
(goog-define alert-timeout-ms 3000)

;; Variables are output from terraform script
(def cognito-config
  {:region                  "us-east-1"
   :base-url                "https://bw47ni63qk.execute-api.us-east-1.amazonaws.com/dev"
   :user-pool               "us-east-1_epddWZIYg"
   :user-pool-arn           "arn:aws:cognito-idp:us-east-1:155889952199:userpool/us-east-1_epddWZIYg"
   :user-pool-client        "1qvefi5absjkrvl23qh6oce0uk"
   })

;; We will refresh auth tokens this many minutes before they are set to expire.
(def token-refresh-offset-min 5)

;; ====
(goog-define app-debugger-active? true)

(when app-debugger-active?
  (.log js/console "Debug Route Is Available At: #/debug"))

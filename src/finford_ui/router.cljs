(ns finford-ui.router
  (:require
    [reitit.frontend :as rfront]
    [finford-ui.events :as shared-events]
    [finford-ui.routes.index.view :as index]
    [finford-ui.routes.debug.view :as debug]
    [finford-ui.routes.debug.events :as debug-events]
    [finford-ui.routes.auth.register.view :as register]
    [finford-ui.routes.auth.login.view :as login]
    [finford-ui.routes.auth.lost-pass.view :as lost-pass]
    [finford-ui.routes.auth.change-pass.view :as change-pass]
    [finford-ui.routes.auth.confirm-account.view :as confirm-account]
    [finford-ui.routes.auth.confirm-account.events :as confirm-account-events]
    [finford-ui.routes.auth.lost-pass.events :as lost-pass-events]
    [finford-ui.routes.accounts.view :as accounts]))

;; == ROUTE DEFINITIONS ==
;; Every route will consist of a vector with the first element being a string
;; representing the path, and an options map. The map at a minimum will contain
;; a `:name` and `:view`. Optionally `:dispatch-on-entry` key can be
;; provided which contains a vector of event vectors to dispatch when the route
;; is entered. These event vectors can include the keys `:query-params` and/or
;; `:path-params` as needed which will be replaced with the actual query and
;; path parameter maps when the event is dispatched.
(def routes
  (rfront/router
    ["/"
     [""                {:name :index
                         :view [index/template]}]
     ["debug"           {:name :debug
                         :view [debug/template]
                         :dispatch-on-entry [[::debug-events/open-broadcast-channel]]}]
     ["register"        {:name :register
                         :view [register/template]}]
     ["login"           {:name :login
                         :view [login/template]}]
     ["lost-pass"       {:name :lost-pass
                         :view [lost-pass/template]
                         :dispatch-on-entry [[::lost-pass-events/add-username :query-params]]}]
     ["change-pass"     {:name :change-pass
                         :authenticated true
                         :view [change-pass/template]}]
     ["confirm-account" {:name :confirm-account
                         :view [confirm-account/template]
                         :dispatch-on-entry [[::confirm-account-events/confirm-registration :query-params]]}]
     ["accounts"        {:name :accounts
                         :authenticated true
                         :view [accounts/template]}]]))

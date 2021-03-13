(ns finford-ui.routes.auth.lost-pass.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::code-sent?
  (fn [db _]
    (-> db :routes :lost-pass :code-sent?)))

(rf/reg-sub
  ::username?
  (fn [db _]
    (-> db :routes :lost-pass :lost-pass-request-form :username :value)))

(ns finford-ui.routes.auth.login.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::login-form-password-visible?
  (fn [db _]
    (-> db :routes :login :password-visible?)))

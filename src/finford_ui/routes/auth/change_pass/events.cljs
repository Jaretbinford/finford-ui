(ns finford-ui.routes.auth.change-pass.events
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [finford-ui.config :as config]
    [finford-ui.utils.helpers :refer [>dis] :as helpers]
    [finford-ui.interceptors :refer [standard-interceptors]]
    [finford-ui.events :as shared-events]))

(defmethod shared-events/input-error? :change-pass-form-old-pass [{:keys [value dirty?]}]
  (and dirty?
       (or
         (string/blank? value)
         (<= (count value) 7))))

(rf/reg-event-fx
  ::successful-password-change
  [standard-interceptors]
  (fn [{:keys [db]} [_ uuid]]
    {:db (-> db
             (assoc-in [:routes :change-pass :change-pass-form :password :value] "")
             (assoc-in [:routes :change-pass :change-pass-form :old-pass :value] ""))
     :dispatch [::shared-events/add-alert uuid "Password successfully changed" "success"]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

(rf/reg-fx
  ::change-cognito-password
  (fn [[cognito-user uuid old-pass new-pass]]
    (.changePassword cognito-user old-pass new-pass (fn [error response]
                                                      (if error
                                                        (>dis [::shared-events/add-alert uuid (-> error (js->clj :keywordize-keys true) :message) "danger"])
                                                        (>dis [::successful-password-change uuid]))))))

(rf/reg-event-fx
  ::change-password
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)]
  (fn [{:keys [db uuid]} _]
    (let [old-pass (-> db :routes :change-pass :change-pass-form :old-pass :value)
          new-pass (-> db :routes :change-pass :change-pass-form :password :value)
          authed-user (-> db :user :authenticated-cognito-user)]
      {::change-cognito-password [authed-user uuid old-pass new-pass]
       :dispatch-later [{:ms config/alert-timeout-ms
                         :dispatch [::shared-events/remove-alert uuid]}]})))

(rf/reg-event-fx
  ::reauthenticate
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)]
  (fn [{:keys [uuid db]} _]
    {:db (assoc db :requested-auth-route "/change-pass")
     ::shared-events/update-url-hash "/login"
     :dispatch       [::shared-events/add-alert uuid "Reauthentication required for password change" "info"]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

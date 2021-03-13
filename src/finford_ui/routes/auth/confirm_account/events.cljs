(ns finford-ui.routes.auth.confirm-account.events
  (:require
    [finford-ui.utils.helpers :refer [>dis] :as helpers]
    [finford-ui.interceptors :refer [standard-interceptors]]
    [finford-ui.events :as shared-events]
    [finford-ui.config :as config]
    [re-frame.core :as rf]))

(rf/reg-fx
  ::confirm-registration
  (fn [[cognito-user uuid code]]
    (.confirmRegistration cognito-user code true (fn [error response]
                                                   (if error
                                                     (>dis [::shared-events/add-alert uuid (-> error (js->clj :keywordize-keys true) :message) "danger"])
                                                     (do
                                                       (>dis [::shared-events/add-alert uuid "Confirmation Successful! You may now login." "success"])
                                                       (>dis [::shared-events/set-url-hash "/login"])))))))

(rf/reg-event-fx
  ::confirm-registration
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)]
  (fn [{:keys [uuid]} [_ {:keys [username code]}]]
    (let [cognito-user (helpers/new-cognito-user username)]
      {::confirm-registration [cognito-user uuid code]
       :dispatch-later [{:ms config/alert-timeout-ms
                         :dispatch [::shared-events/remove-alert uuid]}]})))

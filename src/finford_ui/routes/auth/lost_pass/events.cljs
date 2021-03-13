(ns finford-ui.routes.auth.lost-pass.events
  (:require
    [re-frame.core :as rf]
    [finford-ui.events :as shared-events]
    [finford-ui.routes.auth.lost-pass.db :as route-db]
    [finford-ui.utils.helpers :refer [<sub >dis] :as helpers]
    [finford-ui.config :as config]
    [finford-ui.interceptors :refer [standard-interceptors]]))

(rf/reg-event-fx
  ::successful-request-reset
  [standard-interceptors]
  (fn [{:keys [db]} [_ uuid]]
    {:db (-> db
           (assoc-in [:routes :lost-pass :code-sent?] true)
           (assoc-in [:routes :lost-pass :lost-pass-request-form :username :value] nil))
     :dispatch [::shared-events/add-alert uuid "Reset code and instructions have been emailed." "success"]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

; Usage: In the returned map of a reg-event-fx {::request-reset [cognito-user uuid]}
; Description: Request a password reset from cognito for the provided user. Creating an alert on failure
(rf/reg-fx
  ::request-reset
  (fn [[cognito-user uuid]]
    (.forgotPassword cognito-user (clj->js {:onSuccess #(>dis [::successful-request-reset uuid])
                                            :onFailure #(>dis [::shared-events/add-alert uuid (-> % (js->clj :keywordize-keys true) :message) "danger"])}))))

; Usage: (>dis [::route-events/request-password-reset])
; Description: Request password reset from cognito. Will result in an email
;              being sent to the user with a verification link and code
(rf/reg-event-fx
  ::request-password-reset
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)
   (rf/inject-cofx ::shared-events/unauthenticated-cognito-user)]
  (fn [{:keys [uuid unauthenticated-cognito-user]} _]
    {::request-reset [unauthenticated-cognito-user uuid]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

(rf/reg-event-fx
  ::add-username
  [standard-interceptors]
  (fn [{:keys [db]} [_ {:keys [username code]}]]
    (if code
      {:db (-> db
               ;; NOTE Maybe this should be it's own route instead of double duty? Maybe not :shrug:
               (assoc-in [:routes :lost-pass :code-sent?] true)
               (assoc-in [:routes :lost-pass :lost-pass-request-form :username :value] username)
               (assoc-in [:routes :lost-pass :lost-pass-reset-form :reset-code :value] code))}
      {})))

(rf/reg-event-fx
  ::successful-reset
  [standard-interceptors]
  (fn [{:keys [db]} [_ uuid]]
    {:db (assoc-in db [:routes :lost-pass] route-db/lost-pass-db)
     :dispatch-n [[::shared-events/add-alert uuid "Password successfully reset!" "success"]
                  [::shared-events/set-url-hash "/login"]]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

; Usage: In the returned map of a reg-event-fx {::reset [cognito-user code new-pass uuid]}
; Description:
(rf/reg-fx
  ::reset
  (fn [[cognito-user verfication-code new-pass uuid]]
    (.log js/console "User " cognito-user)
    (.confirmPassword cognito-user verfication-code new-pass (clj->js {:onSuccess #(>dis [::successful-reset uuid])
                                                                       :onFailure #(>dis [::shared-events/add-alert uuid (-> % (js->clj :keywordize-keys true) :message) "danger"])}))))

(rf/reg-event-fx
  ::confirm-password-reset
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)
   (rf/inject-cofx ::shared-events/unauthenticated-cognito-user)]
  (fn [{:keys [uuid unauthenticated-cognito-user db]} _]
    (let [form (-> db :routes :lost-pass :lost-pass-reset-form)
          {:keys [reset-code password]} (reduce (fn [res [k {:keys [value]}]] (assoc res k value)) {} form)]
      {::reset [unauthenticated-cognito-user reset-code password uuid]
       :dispatch-later [{:ms config/alert-timeout-ms
                         :dispatch [::shared-events/remove-alert uuid]}]})))

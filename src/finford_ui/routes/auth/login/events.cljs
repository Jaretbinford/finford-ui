(ns finford-ui.routes.auth.login.events
  (:require
    [re-frame.core :as rf]
    [finford-ui.events :as shared-events]
    [finford-ui.utils.helpers :refer [<sub >dis] :as helpers]
    [finford-ui.config :as config]
    [finford-ui.interceptors :refer [standard-interceptors]]))

(rf/reg-event-db
  ::toggle-password-input-visibility
  [standard-interceptors]
  (fn [db _]
    (update-in db [:routes :login :password-visible?] not)))

; Usage: (>dis [::successful-login login-response authenticated-cognito-user])
; Description: In app-db we reset the login form, associate the `authenticated-cognito-user`
;              in the appropriate spot and reset the `requested-auth-route` to nil.
;              Dispatch welcome message and `set-tokens` with the tokens received in response.
;              Update the URL to either the requested authenticated route or index.
;              Dispatch later event to clear welcome message
(rf/reg-event-fx
  ::successful-login
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)]
  (fn [{:keys [db uuid]} [_ ^js response authenticated-cognito-user]]
    (let [requested-auth-route (:requested-auth-route db)
          username (-> response .getIdToken .getJwtToken helpers/decode-token :cognito:username)]
      {:db (-> db
               (assoc-in [:routes :login :login-form :password :value] nil)
               (assoc-in [:routes :login :login-form :username :value] nil)
               (assoc-in [:user :authenticated-cognito-user] authenticated-cognito-user)
               (assoc :requested-auth-route nil))
       :dispatch-n [[::shared-events/add-alert uuid (str "Welcome: " username) "success"]
                    [::shared-events/set-tokens {:id (-> response .getIdToken .getJwtToken)
                                                 :refresh (-> response .getRefreshToken .getToken)}]]
       ::shared-events/update-url-hash (or requested-auth-route "/")
       :dispatch-later [{:ms config/alert-timeout-ms
                         :dispatch [::shared-events/remove-alert uuid]}]})))

(rf/reg-fx
  ::authenticate-user
  (fn [[^js cognito-user auth-details uuid]]
    (.authenticateUser cognito-user auth-details (clj->js {:onSuccess #(>dis [::successful-login % cognito-user])
                                                           :onFailure #(>dis [::shared-events/add-alert uuid (-> % (js->clj :keywordize-keys true) :message) "danger"])}))))

; Usage: (>dis [::route-events/login])
; Description: Do the cognito authentication dance
(rf/reg-event-fx
  ::login
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)
   (rf/inject-cofx ::shared-events/unauthenticated-cognito-user)
   (rf/inject-cofx ::shared-events/cognito-auth-details)]
  (fn [{:keys [uuid unauthenticated-cognito-user cognito-auth-details]} _]
    {::authenticate-user [unauthenticated-cognito-user cognito-auth-details uuid]}))

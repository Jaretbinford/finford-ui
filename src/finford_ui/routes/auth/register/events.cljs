(ns finford-ui.routes.auth.register.events
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as string]
    [re-frame.core :as rf]
    [finford-ui.config :as config]
    [finford-ui.db :as shared-db]
    [finford-ui.routes.auth.register.db :as route-db]
    [finford-ui.events :as shared-events]
    [finford-ui.utils.helpers :refer [<sub >dis] :as helpers]
    [finford-ui.interceptors :refer [standard-interceptors]]))

(rf/reg-event-fx
  ::successful-sign-up
  [standard-interceptors]
  (fn [{:keys [db]} [_ uuid email]]
    {:db (assoc-in db [:routes :register] route-db/register-db)
     :dispatch [::shared-events/add-alert uuid (str "Success! Confirmation email sent to: " email) "success"]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

(rf/reg-fx
  ::sign-up
  (fn [[uuid username password data-email user-pool]]
    (let [attr-list (clj->js [(new js/AmazonCognitoIdentity.CognitoUserAttribute (clj->js data-email))])]
      (.signUp user-pool username password attr-list nil (fn [error response]
                                                           (if error
                                                            (>dis [::shared-events/add-alert uuid (-> error (js->clj :keywordize-keys true) :message) "danger"])
                                                            (>dis [::successful-sign-up uuid (:Value data-email)])))))))

(rf/reg-event-fx
  ::register-user
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)
   (rf/inject-cofx ::shared-events/user-pool)]
  (fn [{:keys [uuid user-pool db]} _]
    (let [form (-> db :routes :register :registration-form)
          {:keys [username password email]} (reduce (fn [res [k {:keys [value]}]] (assoc res k value)) {} form)]
      {::sign-up [uuid username password {:Name "email" :Value email} user-pool]
       :dispatch-later [{:ms config/alert-timeout-ms
                         :dispatch [::shared-events/remove-alert uuid]}]})))

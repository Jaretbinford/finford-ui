(ns finford-ui.routes.accounts.events
  (:require [re-frame.core :as rf]
            [finford-ui.events :as shared-events]
            [finford-ui.config :as config]
            [finford-ui.interceptors :refer [standard-interceptors]]))

(rf/reg-event-fx
  ::sound-alert
  [standard-interceptors
   (rf/inject-cofx ::shared-events/uuid)]
  (fn [{:keys [uuid]} _]
    {:dispatch [::shared-events/add-alert uuid "AN ALERT HAS BEEN ISSUED" "danger"]
     :dispatch-later [{:ms config/alert-timeout-ms
                       :dispatch [::shared-events/remove-alert uuid]}]}))

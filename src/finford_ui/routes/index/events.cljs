(ns finford-ui.routes.index.events
  (:require
    [re-frame.core :as rf]
    [finford-ui.interceptors :refer [standard-interceptors]]))

(rf/reg-event-db
   ::set-color
   [standard-interceptors]
  (fn [db [_ new-color-value]]
    (assoc-in db [:routes :index :hex-color] new-color-value)))

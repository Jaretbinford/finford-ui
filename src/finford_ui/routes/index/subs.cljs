(ns finford-ui.routes.index.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  ::hex-color
  (fn [db _]
    (get-in db [:routes :index :hex-color])))

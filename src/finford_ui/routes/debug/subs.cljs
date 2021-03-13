(ns finford-ui.routes.debug.subs
  (:require
    [cljs.reader :refer [read-string]]
    [lambdaisland.deep-diff2 :as ddiff]
    [re-frame.core :as rf]))

(rf/reg-sub
  ::evetnt-db
  (fn [db _]
    (let [path (get-in db [:routes :debug :db-path])
          event-n (-> db :routes :debug :selected-event)
          events (-> db :routes :debug :events)
          app-db (-> (filter #(= (js/parseInt event-n) (:position %)) events)
                     first
                     :db)]
      (if app-db
        (get-in app-db path app-db)
        :no-db-for-event))))

(rf/reg-sub
  ::events
  (fn [db _]
    (get-in db [:routes :debug :events] [])))

(rf/reg-sub
  ::selected-event
  (fn [db _]
    (let [event-n (-> db :routes :debug :selected-event)
          events (-> db :routes :debug :events)]
      (-> (filter #(= (js/parseInt event-n) (:position %)) events)
          first
          :event))))

(rf/reg-sub
  ::node-keys
  (fn [db _]
    (let [path (get-in db [:routes :debug :db-path])
          event-n (-> db :routes :debug :selected-event)
          events (-> db :routes :debug :events)
          app-db (-> (filter #(= (js/parseInt event-n) (:position %)) events)
                     first
                     :db)
          node (get-in app-db path app-db)]
      (when (map? node)
        (keys node)))))

(rf/reg-sub
  ::db-path
  (fn [db _]
    (get-in db [:routes :debug :db-path])))

(rf/reg-sub
  ::selected-event-n
  (fn [db _]
    (get-in db [:routes :debug :selected-event] nil)))

(rf/reg-sub
  ::db-diff-from-previous-event
  (fn [db _]
    (let [path (get-in db [:routes :debug :db-path])
          event-n (-> db :routes :debug :selected-event)
          events (-> db :routes :debug :events)
          app-db (-> (filter #(= (js/parseInt event-n) (:position %)) events)
                     first
                     :db)
          previous-app-db (-> (filter #(= (dec (js/parseInt event-n)) (:position %)) events)
                              first
                              :db)]
      (when (and app-db previous-app-db)
        (ddiff/diff (get-in previous-app-db path previous-app-db) (get-in app-db path app-db) )))))

(rf/reg-sub
  ::show-diff?
  (fn [db _]
    (-> db :routes :debug :show-diff?)))

(ns finford-ui.routes.debug.events
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [cognitect.transit :as t]
    [clojure.string :as str]
    [finford-ui.events :as shared-events]
    [finford-ui.config :as config]
    [finford-ui.utils.helpers :refer [>dis]]))

(def max-events 15)
(defonce debug-chan (r/atom {}))

(rf/reg-event-fx
  ::add-event
  (fn [{:keys [db]} [_ event]]
    (let [current-events (get-in db [:routes :debug :events] [])
          trimmed-events (if (= max-events (-> current-events last :position))
                           (rest current-events)
                           current-events)
          updated-events (mapcat seq [trimmed-events [event]])
          numbered-events (map-indexed #(assoc %2 :position (inc %1)) updated-events)]
      {:db (-> db
               (assoc-in [:routes :debug :events] numbered-events)
               (assoc-in [:routes :debug :selected-event] (-> numbered-events count str)))})))

(rf/reg-fx
  ::init-broadcast-channel
  (fn []
    (let [not-started? (nil? (:channel @debug-chan))
          channel (when not-started? (js/BroadcastChannel. "debugChan"))]
      (when not-started?
        (swap! debug-chan assoc :channel channel)
        (set! (.-onmessage channel) (fn [message]
                                      (let [r (t/reader :json)
                                            data (t/read r (.-data message))]
                                        (>dis [::add-event data]))))))))

(rf/reg-event-fx
  ::open-broadcast-channel
  (fn [_ _]
    {::init-broadcast-channel []}))

(rf/reg-event-fx
  ::add-path-key
  (fn [{:keys [db]} [_ k]]
    {:db (update-in db [:routes :debug :db-path] (fn [path]
                                                   (if path
                                                     (merge path k)
                                                     [k])))}))

(rf/reg-event-fx
  ::select-event
  (fn [{:keys [db]} [_ e]]
    {:db (assoc-in db [:routes :debug :selected-event] e)}))

(rf/reg-event-fx
  ::dec-event
  (fn [{:keys [db]} _]
    {:db (update-in db [:routes :debug :selected-event] #(-> % js/parseInt dec str))}))

(rf/reg-event-fx
  ::inc-event
  (fn [{:keys [db]} _]
    {:db (update-in db [:routes :debug :selected-event] #(-> % js/parseInt inc str))}))

(rf/reg-event-fx
  ::remove-db-path-kw
  (fn [{:keys [db]} [_ kw]]
    (let [current-path (-> db :routes :debug :db-path reverse)
          path-less-segments (drop-while #(not= kw %) current-path)
          new-path (-> path-less-segments rest reverse vec)]
      {:db (assoc-in db [:routes :debug :db-path] new-path)})))

(rf/reg-event-db
  ::toggle-show-diff?
  (fn [db _]
    (update-in db [:routes :debug :show-diff?] not)))

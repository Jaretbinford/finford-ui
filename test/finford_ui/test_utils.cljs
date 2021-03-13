(ns finford-ui.test-utils
  (:require
    [re-frame.core :as rf]
    [finford-ui.events :as shared-events]))

;; -- Test Fixtures ------------------------------------------------------------
(defn stub-local-store
  "localStore does not function properly in headless chrome tests"
  []
  (rf/reg-cofx
  ::shared-events/local-store
  (fn [cofx _]
    (-> cofx
        (assoc :ls-tokens {:id nil
                           :refresh nil})
        (assoc :ls-user {:uuid nil
                         :permissions nil
                         :email nil
                         :username nil
                         :authenticated-cognito-user nil})))))

(defn monkey-patch-local-store
  "localStore does not function properly in headless chrome tests"
  [ls]
  (rf/reg-fx
    ::shared-events/save-in-local-storage
    (fn [map-to-store]
      (doseq [[k v] map-to-store]
        (swap! ls assoc k v))))
  (rf/reg-fx
    ::shared-events/remove-from-local-storage
    (fn [keys]
      (doseq [k keys]
        (swap! ls dissoc k)))))

(defn stub-and-monkey-patch-local-store [ls]
  (stub-local-store)
  (monkey-patch-local-store ls))

(ns finford-ui.components.alerts
  (:require
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.subs :as shared-subs]
    [finford-ui.events :as shared-events]))

(defn alert-display []
  (let [alerts (<sub [::shared-subs/alerts])]
    [:div#alerts
     (for [alert alerts]
       [:div.notification {:key (:uuid alert)
                           :class (str "is-" (:type alert))}
        (:message alert)
        [:span {:on-click #(>dis [::shared-events/remove-alert (:uuid alert)])} "X"]])]))

(ns finford-ui.routes.accounts.view
  (:require
    [finford-ui.components.nav :refer [nav-bar]]
    [finford-ui.utils.helpers :refer [>dis]]
    [finford-ui.routes.accounts.events :as route-events]))

(defn template []
  [:<>
   [nav-bar]
   [:div
    [:button
     {:on-click #(>dis [::route-events/sound-alert])}
     "Sound The Alert"]]])






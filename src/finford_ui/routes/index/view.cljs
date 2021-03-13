(ns finford-ui.routes.index.view
  (:require
    [finford-ui.components.nav :refer [nav-bar]]
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.routes.index.subs :as route-subs]
    [finford-ui.routes.index.events :as route-events]))

(defn color-display []
  (let [rgb (<sub [::route-subs/hex-color])]
    [:h1 "The color is now: " [:span {:style {:color rgb}} rgb]]))

(defn color-input []
  [:div.color-input
   "Color: "
   [:input {:type "text"
            :value (<sub [::route-subs/hex-color])
            :on-change #(>dis [::route-events/set-color (-> % .-target .-value)])}]])

(defn template []
  [:<>
   [nav-bar]
   [:div
    [:h2 "Color Input"]
    [color-display]
    [color-input]]])

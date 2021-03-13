(ns finford-ui.routes.debug.view
  (:require
    [reagent.core :as r]
    [reagent.dom :as r-dom]
    ["highlight.js" :as hljs]
    [finford-ui.utils.helpers :refer [>dis <sub]]
    [finford-ui.routes.debug.events :as route-events]
    [finford-ui.routes.debug.subs :as route-subs]))

(defn highlighted-code-block [db]
  (let [highlight-fn (fn [this]
                       (let [el (r-dom/dom-node this)
                             _ (.highlightBlock hljs el)
                             all-span-els (array-seq (.querySelectorAll el "span"))]
                         (doseq [span-el all-span-els]
                           (cond
                             (= ":-" (.-innerText span-el)) (.setAttribute span-el "class" "removed-value")
                             (= ":+" (.-innerText span-el)) (.setAttribute span-el "class" "added-value")))
                         el))]
    (r/create-class {:reagent-render (fn [cb]
                                       [:code {:class "Clojure clojure"} (with-out-str (cljs.pprint/pprint cb))])
                     :component-did-mount highlight-fn
                     :component-did-update highlight-fn})))

(defn node-keys []
  (let [the-keys (<sub [::route-subs/node-keys])]
    [:div#node-keys
     (for [k the-keys]
       [:p.card {:key k
                 :on-click #(>dis [::route-events/add-path-key k])}
        (str k)])]))

(defn event-card []
  (let [args-visible? (r/atom false)]
    (fn []
      (let [[event args] (<sub [::route-subs/selected-event])]
        (when event
          [:div#event-card.card
           [:div#event-name
            [:p.title (str event)]
            [:p.icon {:on-click #(swap! args-visible? not)} (if @args-visible? "▼" "▶")]]
           (when @args-visible?
             [:div#arg-code-block
              [:pre [highlighted-code-block args]]])])))))

(defn event-selector []
  (let [the-events (<sub [::route-subs/events])
        selected-event-n (<sub [::route-subs/selected-event-n])]
    [:div#event-selector
     [:div.event-arrow {:on-click #(>dis [::route-events/dec-event])} [:p "◀"]]
     [:div#select-box-div
      [:div.select-box
       [:select {:value selected-event-n
                 :on-change #(>dis [::route-events/select-event (-> % .-target .-value)])}
        [:option {:default-value true} "-- Select Event --"]
        (for [{:keys [position event]} the-events]
          [:option {:key position :value position} (str position ": " (-> event first name))])]]]
     [:div.event-arrow {:on-click #(>dis [::route-events/inc-event])} [:p "▶"]]]))

(defn db-path []
  (let [path (<sub [::route-subs/db-path])]
    (when (not-empty path)
      [:div#db-path-segments
       [:ul
        (for [kw path]
          [:li {:key (random-uuid)} [:a {:on-click #(>dis [::route-events/remove-db-path-kw kw])} (str kw)]])]])))

(defn db-state []
  (let [db (<sub [::route-subs/evetnt-db])
        diff? (<sub [::route-subs/show-diff?])
        diff (<sub [::route-subs/db-diff-from-previous-event])]
    (if (= :no-db-for-event db)
      [:div#no-db "No DB was associated with event"]
      [:div#db-card.card
       [:div.title
        [:h2 "DB State After"]
        [:p "Diff: " [:span#diff-toggle {:on-click #(>dis [::route-events/toggle-show-diff?])} (if diff? "True" "False")]]]
       [:div#db-card-grid
        [db-path]
        [node-keys]
        (if diff?
          [:pre [highlighted-code-block diff]]
          [:pre [highlighted-code-block db]])]])))

(defn template []
    [:div#debug-template
     [:div
      [event-selector]
      [event-card]
      [db-state]]])

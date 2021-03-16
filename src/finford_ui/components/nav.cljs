(ns finford-ui.components.nav
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.events :as shared-events]
    [finford-ui.subs :as shared-subs]))

;Read Reagent docs.
;reframe does state management

(rf/reg-event-db
  ::toggle-mobile-nav-modal
  (fn [db _]
    (update-in db [:mobile-nav-visible?] not)))

(rf/reg-sub
  ::mobile-nav-visible?
  (fn [db]
    (:mobile-nav-visible? db)))

(defn mobile-nav-bar []
  (let [visible? (<sub [::mobile-nav-visible?])]
    [:div.modal-overlay (when visible? {:class "is-visible"})
     [:div#mobile-nav-items
      [:ul.menu
       [:li.menu-item [:a {:href "#/"} "Index"]]
       [:li.menu-item [:a {:href "#/accounts"} "Accounts Snapshot"]]
       [:li.menu-item [:a {:href "#/change-pass"} "Change Password"]]]]]))

(defn nav-bar []
  (let [mobile-nav-visible? (<sub [::mobile-nav-visible?])
        {:keys [username]} (<sub [::shared-subs/logged-in-user])]
    [:<>
     [mobile-nav-bar]
     [:header
      [:div.logo [:img {:src "https://raw.githubusercontent.com/jr0cket/developer-guides/master/clojure/clojure-bank-coin.png"}]]
      [:nav.hidden-on-mobile
       [:ul.nav-links
        [:li#login-button [:a {:href "#/"} "Index"]]
        [:li#login-button [:a {:href "#/accounts"} "Accounts Snapshot"]]
        [:li#login-button [:a {:href "#/upload"} "Dashboard"]]
        [:li#login-button [:a {:href "#/upload"} "Upload"]]
        [:li#login-button [:a {:href "#/change-pass"} "Change Password"]
      (if username
         [:span#login-main [:a {:href "#/login" :on-click #(>dis [::shared-events/logout])} "Logout"]]
         [:span#login-main [:a {:href "#/login"} "Login"]])]
      [:a.visible-on-mobile.hamburger {:id (when mobile-nav-visible? "mobile-menu-open")
                                       :on-click #(>dis [::toggle-mobile-nav-modal])} [:span]]]]]]))

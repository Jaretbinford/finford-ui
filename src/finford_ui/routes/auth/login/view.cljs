(ns finford-ui.routes.auth.login.view
  (:require
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.components.validated-input :as validated-input]
    [finford-ui.events :as shared-events]
    [finford-ui.subs :as shared-subs]
    [finford-ui.routes.auth.login.subs :as route-subs]
    [finford-ui.routes.auth.login.events :as route-events]))

(defn username-input-box []
  [validated-input/text {:id "login-username-input"
                         :label "Username "
                         :route :login
                         :form :login-form
                         :field :username}])

(defn password-input-box []
  (let [password-visible? (<sub [::route-subs/login-form-password-visible?])]
    [validated-input/text {:id "login-password-input"
                           :type (if password-visible? "text" "password")
                           :label "Password "
                           :route :login
                           :form :login-form
                           :field :password}]))

(defn toggle-password-visibility-button []
  (let [password-visible? (<sub [::route-subs/login-form-password-visible?])]
    [:a {:on-click #(>dis [::route-events/toggle-password-input-visibility])}
    (if password-visible? [:img {:src "https://raw.githubusercontent.com/Jaretbinford/finford-ui/master/resources/images/noshow.png" :width "50" :height "25"}] [:img {:src "https://raw.githubusercontent.com/Jaretbinford/finford-ui/75f4ec39e638ed52647f8919e5ee8f79efe2209d/resources/images/unlockshow.png" :width "50" :height "25"}])]))

(defn log-in-button []
  [:div [:button#login-button
         {:type "submit"
          :on-click (fn [evt]
                              (.preventDefault evt)
                              (>dis [::shared-events/submit-if-form-valid
                                     :login
                                     :login-form
                                     [::route-events/login]]))}
         "Log in"]])

(defn template []
  (let [{:keys [username password]} (<sub [::shared-subs/form :login :login-form])]
    [:div#login-wrapper
     [:form
      [:div#username
       [username-input-box]]
      [:div#password
       [password-input-box]]
      [:div
       [toggle-password-visibility-button]]
       [log-in-button]]
     [:div
      [:a {:href "#/lost-pass"} "Lost Password "] [:a {:href "#/register"} "Register"]]
]))

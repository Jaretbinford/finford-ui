(ns finford-ui.routes.auth.register.view
 (:require
    [finford-ui.utils.helpers :as utils]
    [finford-ui.subs :as shared-subs]
    [finford-ui.events :as shared-events]
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.components.validated-input :as validated-input]
    [finford-ui.routes.auth.register.events :as route-events]))

; region = Components ===============================================================
(defn username-input-box []
  [validated-input/text {:id "registration-username-input"
                         :label "Username"
                         :route :register
                         :form :registration-form
                         :field :username}])

(defn email-input-box []
  [validated-input/text {:id "registration-email-input"
                         :label "Email"
                         :route :register
                         :form :registration-form
                         :field :email}])

(defn password-input-box []
  [validated-input/text {:id "registration-password-input"
                         :label "Password"
                         :route :register
                         :form :registration-form
                         :field :password}])

(defn password-confirm-input-box []
  [validated-input/text {:id "registration-confirm-password-input"
                         :label "Confirm Password"
                         :route :register
                         :form :registration-form
                         :field :password-confirm}])

(defn register-button []
  [:button#register-button {:type "submit"
                            :on-click (fn [evt]
                                        (.preventDefault evt)
                                        (>dis [::shared-events/submit-if-form-valid :register :registration-form [::route-events/register-user]]))}
   "Register"])
; endregion

; region = Route ======================================================================
(defn template []
  [:div#registration-wrapper
   [:h1 "Register"]
   [:form
    [:div
     [username-input-box]]
    [:div
     [email-input-box]]
    [:div
     [password-input-box]]
    [:div
     [password-confirm-input-box]]
    [register-button]]])
; endregion

(ns finford-ui.routes.auth.lost-pass.view
  (:require
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.components.validated-input :as validated-input]
    [finford-ui.events :as shared-events]
    [finford-ui.subs :as shared-subs]
    [finford-ui.routes.auth.lost-pass.subs :as route-subs]
    [finford-ui.routes.auth.lost-pass.events :as route-events]))

;; ====
;  Request reset
(defn username-input-box []
  [validated-input/text {:id "lost-pass-username-input"
                         :label "Username or Email"
                         :route :lost-pass
                         :form :lost-pass-request-form
                         :field :username}])

(defn request-reset-button []
  [:div [:button {:type "submit"
                  :on-click (fn [evt]
                              (.preventDefault evt)
                              (>dis [::shared-events/submit-if-form-valid
                                     :lost-pass
                                     :lost-pass-request-form
                                     [::route-events/request-password-reset]]))}
         "Request Reset"]])

;; ====
;  Confirm reset
(defn code-input-box []
  [validated-input/text {:id "lost-pass-reset-code-input"
                         :label "Reset Code From Email"
                         :route :lost-pass
                         :form :lost-pass-reset-form
                         :field :reset-code}])

(defn password-input-box []
  [validated-input/text {:id "lost-pass-new-password-input"
                         :label "New Password"
                         :route :lost-pass
                         :form :lost-pass-reset-form
                         :field :password}])

(defn reset-button []
  [:div [:button {:type "submit"
                  :on-click (fn [evt]
                              (.preventDefault evt)
                              (>dis [::shared-events/submit-if-form-valid
                                     :lost-pass
                                     :lost-pass-reset-form
                                     [::route-events/confirm-password-reset]]))}
         "Reset Password"]])

(defn template []
  (let [code-sent? (<sub [::route-subs/code-sent?])
        user-name? (<sub [::route-subs/username?])]
    (cond (not code-sent?) [:div#password-reset-request-wrapper
                            [:form.form
                             [:h3.form-title "Request Reset"]
                             [username-input-box]
                             [request-reset-button]]]
          (and code-sent? (not user-name?)) [:div#password-reset-request-success
                                             [:form.form
                                              [:h3.form-title "Email Should Be In Route"]]]
          (and code-sent? user-name?) [:div#password-reset-wrapper
                                       [:form.form
                                        [:h3.form-title "Set New Password"]
                                        [:div
                                         [code-input-box]]
                                        [:div
                                         [password-input-box]]
                                        [reset-button]]])))

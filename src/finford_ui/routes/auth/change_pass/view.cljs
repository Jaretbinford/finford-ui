(ns finford-ui.routes.auth.change-pass.view
  (:require
    [finford-ui.components.validated-input :as validated-input]
    [finford-ui.subs :as shared-subs]
    [finford-ui.events :as shared-events]
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.routes.auth.change-pass.events :as route-events]))

(defn old-pass-input-box []
  [validated-input/text {:id "change-pass-old-password"
                         :label "Old Password"
                         :route :change-pass
                         :form :change-pass-form
                         :field :old-pass}])

(defn new-pass-input-box []
  [validated-input/text {:id "change-pass-new-password"
                         :label "New Password"
                         :route :change-pass
                         :form :change-pass-form
                         :field :password}])

(defn change-password-button []
  [:div [:button {:type "submit"
                  :on-click (fn [evt]
                              (.preventDefault evt)
                              (>dis [::shared-events/submit-if-form-valid
                                     :change-pass
                                     :change-pass-form
                                     [::route-events/change-password]]))}
         "Change Password"]])

;; If there is no registered-user object. We are going to need the user to
;  sign in again. This happens when there has been a hard page refresh as I
;  haven't worked out how to save a authenticated user object in localStorage
;  and then rehydrate it after a refresh
(defn template []
  (let [has-authenticated-user? (<sub [::shared-subs/has-authenticated-cognito-user?])
        {:keys [old-pass password]} (<sub [::shared-subs/form :change-pass :change-pass-form])]
    (if has-authenticated-user?
      [:div#change-pass-wrapper
       [:form
        [:div
         [:div
          [old-pass-input-box]]
         [:div
          [new-pass-input-box]]
         [change-password-button]]]]
      (>dis [::route-events/reauthenticate]))))

(ns finford-ui.components.validated-input
  (:require
    [finford-ui.utils.helpers :refer [<sub >dis]]
    [finford-ui.subs :as shared-subs]
    [finford-ui.events :as shared-events]))

(defn text [{:keys [id type label route form field additional-dom]
             :or {type "text"}}]
  (let [{:keys [has-error? error-message value]} (<sub [::shared-subs/form-field route form field])]
    [:div.field
     [:label {:for id} label]
     (when additional-dom
       additional-dom)
     [:input.input {:type type
                    :id id
                    :class (when has-error? "input-error")
                    :value value
                    :placeholder label
                    :on-change #(>dis [::shared-events/set-form-field-value route form field (-> % .-target .-value)])
                    :on-blur #(>dis [::shared-events/set-form-field-dirty route form field])}]
     (when has-error? [:span.error-message error-message])]))

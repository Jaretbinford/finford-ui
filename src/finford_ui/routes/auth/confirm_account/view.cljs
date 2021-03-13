(ns finford-ui.routes.auth.confirm-account.view)

(defn template
  "This should not be visible for long, as the redirect should happen almost instantly"
  []
  [:div
   [:h1 "Your account is being confirmed..."]])

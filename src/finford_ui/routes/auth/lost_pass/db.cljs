(ns finford-ui.routes.auth.lost-pass.db
  (:require
    [clojure.spec.alpha :as s]
    [finford-ui.specs :as specs]
    [finford-ui.utils.helpers :as utils]))

(s/def ::code-sent? boolean?)
(s/def ::lost-pass-request-form ::specs/form)
(s/def ::lost-pass-reset-form ::specs/form)

(s/def ::lost-pass-db (s/keys :req-un [::code-sent? ::lost-pass-request-form ::lost-pass-reset-form]))

(defonce lost-pass-db {:code-sent? false
                       :lost-pass-request-form {:username (utils/empty-form-field {:error-message "You must provide a valid username or email"
                                                                                   :validator-fn :shared/non-empty-string
                                                                                   :required? true})}
                       :lost-pass-reset-form {:password (utils/empty-form-field {:error-message "Password must be at least 8 characters long containing a number, uppercase and lowercase letter"
                                                                                 :validator-fn :shared/password
                                                                                 :required? true})
                                              :reset-code (utils/empty-form-field {:error-message "Enter the 6 digit reset code"
                                                                                   :validator-fn :lost-pass-form/reset-code
                                                                                   :required? true})}})

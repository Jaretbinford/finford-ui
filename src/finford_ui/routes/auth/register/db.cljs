(ns finford-ui.routes.auth.register.db
  (:require
    [clojure.spec.alpha :as s]
    [finford-ui.specs :as specs]
    [finford-ui.utils.helpers :as utils]))

(s/def ::registering-user? boolean?)
(s/def ::registration-form ::specs/form)

(s/def ::register-db (s/keys :req-un [::registering-user?
                                      ::registration-form]))

(defonce register-db {:registering-user? false
                      :registration-form {:username (utils/empty-form-field {:error-message "You must provide a username"
                                                                             :validator-fn :shared/non-empty-string
                                                                             :required? true})
                                          :email    (utils/empty-form-field {:error-message "You must provide a valid email"
                                                                             :validator-fn :shared/non-empty-string
                                                                             :required? true})
                                          :password (utils/empty-form-field {:error-message "Password must be at least 8 characters long containing a number, uppercase and lowercase letter"
                                                                             :validator-fn :shared/password
                                                                             :required? true})
                                          :password-confirm (utils/empty-form-field {:error-message "Your passwords much match"
                                                                                     :validator-fn :registration-form/confirm-password
                                                                                     :required? true})}})

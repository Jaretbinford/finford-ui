(ns finford-ui.routes.auth.login.db
  (:require
    [clojure.spec.alpha :as s]
    [finford-ui.specs :as specs]
    [finford-ui.utils.helpers :as utils]))

(s/def ::password-visible? boolean?)
(s/def ::login-form ::specs/form)
(s/def ::login-db (s/keys :req-un [::login-form
                                   ::password-visible?]))

(defonce login-db {:login-form {:username (utils/empty-form-field {:error-message "You must provide a valid username or email"
                                                                   :validator-fn :shared/non-empty-string
                                                                   :required? true})
                                :password (utils/empty-form-field {:error-message "Password must be at least 8 characters long containing a number, uppercase and lowercase letter"
                                                                   :validator-fn :shared/password
                                                                   :required? true})}
                   :password-visible? false})

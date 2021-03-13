(ns finford-ui.routes.auth.change-pass.db
  (:require
    [clojure.spec.alpha :as s]
    [finford-ui.specs :as specs]
    [finford-ui.utils.helpers :as utils]))

(s/def ::change-pass-form ::specs/form)

(s/def ::change-pass-db (s/keys :req-un [::change-pass-form]))

(defonce change-pass-db {:change-pass-form {:old-pass (utils/empty-form-field {:error-message "Your password should have been at least 8 characters long containing a number, uppercase and lowercase letter"
                                                                               :validator-fn :shared/password
                                                                               :required? true})
                                            :password (utils/empty-form-field {:error-message "Password must be at least 8 characters long containing a number, uppercase and lowercase letter"
                                                                               :validator-fn :shared/password
                                                                               :required? true})}})

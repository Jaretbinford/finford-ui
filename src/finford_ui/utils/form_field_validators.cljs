(ns finford-ui.utils.form-field-validators
  (:require
    [clojure.string :as str]
    [clojure.set :as set]
    [finford-ui.events :refer [input-error?] :as shared-events]))

(defn invalid-password?
  "At least 8 characters with one or more of each uppercase letter lowercase letter and number"
  [value]
  (not (boolean (and
                  (>= (count value) 8)
                  (re-matches #".*[0-9].*" value)
                  (re-matches #".*[A-Z].*" value)
                  (re-matches #".*[a-z].*" value)))))

; region == Shared Validators
(defmethod input-error? :shared/non-empty-string [{:keys [value dirty? required?]}]
  (and dirty?
       required?
       (str/blank? value)))

(defmethod input-error? :shared/password [{:keys [value dirty? required?]}]
  (and dirty?
       required?
       (or
         (str/blank? value)
         (invalid-password? value))))

(defmethod input-error? :shared/email [{:keys [value dirty? required?]}]
  (and dirty?
       required?
       (or
         (str/blank? value)
         (re-matches #"/^(([^<>()\[\]\\.,;:\s@\"]+(\.[^<>()\[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/" value))))

(defmethod input-error? :shared/empty-or-number [{:keys [value dirty? required?]}]
  (and dirty?
       required?
       (and
         (not (str/blank? value))
         (not (re-matches #"\d\.?\d*" value)))))
; endregion

; region == Lost Password Form Validators
(defmethod input-error? :lost-pass-form/reset-code [{:keys [value dirty?]}]
  (and dirty?
       (not (re-matches #"(?:^$|\d{6})" value))))
; endregion

; region == Registration Form Validators
(defmethod input-error? :registration-form/confirm-password [{:keys [value dirty? form]}]
  (let [original-password (-> form :password :value)]
    (and dirty?
       (or
         (str/blank? value)
         (invalid-password? value)
         (not= original-password value)))))
; endregion



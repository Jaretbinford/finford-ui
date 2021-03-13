(ns finford-ui.utils.helpers
  (:require
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as gen]
    [clojure.string :as str]
    [clojure.test.check.generators]
    ["dayjs" :as dayjs]
    [goog.crypt.base64 :as b64]
    [re-frame.core :as rf]

    [finford-ui.config :refer [cognito-config] :as config]))

(defn remove-matching-uuid [elements uuid]
  (remove #(= uuid (:uuid %)) elements))

(def <sub (comp deref rf/subscribe))
(def >dis rf/dispatch)

(defn decode-token
  "Given a jwt isolate and decode the payload returning it as a map"
  [token]
  (some-> token
          (str/split #"\.")
          second
          (b64/decodeString true)
          js/JSON.parse
          (js->clj :keywordize-keys true)))

(defn ms-until-refresh
  "Provided an integer `timestamp` representing the `:exp` of a token, a dayjs
   object representing the current time `now` and an integer specifying a
   number of minutes before the token expiration time. Return an integer
   representing the number of milliseconds until the offset before token
   expiration."
  [timestamp ^js now offset]
  (-> (.unix dayjs timestamp)
      (.diff now)
      (dayjs)
      (.subtract offset "minutes")
      .unix
      (* 1000)))

(defn new-cognito-user
  "Create a new unauthenticated cognito user"
  [username]
  (let [user-pool (new js/AmazonCognitoIdentity.CognitoUserPool (clj->js {:UserPoolId (:user-pool cognito-config)
                                                                          :ClientId (:user-pool-client cognito-config)}))]
    (new js/AmazonCognitoIdentity.CognitoUser (clj->js {:Username username
                                                        :Pool user-pool}))))

(defn empty-form-field
  [{:keys [error-message required? validator-fn]
    :or {validator-fn :shared/non-empty-string}}]
  {:value nil
   :dirty? false
   :validator-fn validator-fn
   :required? required?
   :has-error? false
   :error-message error-message})

(defn valid-jwt? [jwt]
      (re-matches #"^[a-zA-Z0-9\-_]+?\.[a-zA-Z0-9\-_]+?\.([a-zA-Z0-9\-_]+)?$" jwt))

(defn uuid-str-gen []
      (gen/fmap (fn [uuid] (.toString uuid)) (s/gen uuid?)))

(defn valid-uuid-str?
  "Use in JS where UUID functions are limited"
  [uuid-str]
  (re-matches #"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$" uuid-str))

(def non-empty-string-alphanumeric
  "Generator for non-empty alphanumeric strings"
  (gen/such-that #(not= "" %)
                 (gen/string-alphanumeric)))

(def email-gen
  "Generator for email addresses"
  (gen/fmap
    (fn [[name host tld]]
        (str name "@" host "." tld))
    (gen/tuple
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric
      non-empty-string-alphanumeric)))

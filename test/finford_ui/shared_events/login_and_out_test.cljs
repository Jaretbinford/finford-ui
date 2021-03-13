(ns finford-ui.shared-events.login-and-out-test
  (:require
    [dayjs]
    [cljs.test :refer-macros [deftest testing is use-fixtures]]
    [day8.re-frame.test :as rf-test]
    [finford-ui.test-utils :as test-utils]
    [finford-ui.events :as shared-events]
    [finford-ui.config :as config]
    [finford-ui.subs :as shared-subs]
    [goog.crypt.base64 :as b64]
    [re-frame.core :as rf]))

(def ls (atom {}))

(use-fixtures :each {:before (test-utils/stub-and-monkey-patch-local-store ls)
                     :after #(reset! ls {})})

(defn make-token
  "Create a parsable token that has an expiration timestamp of 1 second beyond
   the offset that is defined in config. Allowing testing the refresh request in 1 second."
  []
  (let [token {:email "jarrod@test.com",
               :cognito:username "JarrodCTaylor",
               :exp (-> (dayjs) (.add (+ 1 (* 60 config/token-refresh-offset-min)) "seconds") .unix str)}
        b64-token (-> token clj->js js/JSON.stringify (b64/encodeString))]
    (str "a." b64-token ".b")))

;; ====
;  We want to stub out the focused interactions with the cognito library
(defn stub-request-fresh-tokens [refresh-requested?]
  (rf/reg-event-fx
    ::shared-events/request-fresh-tokens
    (fn [_ _]
      (swap! refresh-requested? not)
      {})))

(defn stub-cognito-user-cofx []
  (rf/reg-cofx
    ::shared-events/unauthenticated-cognito-user
    (fn [cofx _]
      (assoc cofx :cognito-user :noop))))

(defn stub-cognito-signout []
  (rf/reg-fx
    ::shared-events/cognito-signout
    (fn [_] :noop)))

(deftest set-tokens-and-logout
  (rf-test/run-test-async
    (rf/dispatch-sync [::shared-events/initialize-db])
    (let [refresh-requested? (atom false)
          id-token (make-token)
          refresh-token "asdf.zxcv.qwer.sdfg.erty"
          initial-user-state {:uuid nil :permissions nil :username nil :email nil :authenticated-cognito-user nil}
          initial-token-state {:id nil :refresh nil}
          logged-in-token-state {:id id-token :refresh refresh-token}
          logged-in-user-state {:uuid nil
                                :permissions nil
                                :username "JarrodCTaylor"
                                :email "jarrod@test.com"
                                :authenticated-cognito-user nil}
          logged-in-local-storage-state {:finford-ui-cognito-id-token id-token
                                         :finford-ui-cognito-refresh-token refresh-token
                                         :finford-ui-email "jarrod@test.com"
                                         :finford-ui-username "JarrodCTaylor"}
          user (rf/subscribe [::shared-subs/logged-in-user])
          tokens (rf/subscribe [::shared-subs/user-tokens])]
      (stub-request-fresh-tokens refresh-requested?)
      (stub-cognito-user-cofx)
      (stub-cognito-signout)

      (testing "Sanity check initial state"
        (is (= initial-user-state @user) "[initial-user-state] does not contain the desired value")
        (is (= initial-token-state @tokens) "[initial-token-state] does not contain the desired value"))

      (testing "State is appropriately updated and refresh has been requested"
        (rf/dispatch [::shared-events/set-tokens {:id id-token :refresh refresh-token}])
        (rf-test/wait-for [::shared-events/request-fresh-tokens]
                          (is (= logged-in-user-state @user) "[logged-in-user-state] does not contain the desired value")
                          (is (= logged-in-token-state @tokens) "[logged-in-token-state] does not contain the desired value")
                          (is (= logged-in-local-storage-state @ls) "[logged-in-local-storage-state] does not contain the deisred value")
                          (is @refresh-requested? "A fresh token should have been requested.")))

      (testing "Logout event resets all state to initial values"
        (rf/dispatch-sync [::shared-events/logout])
        (is (= initial-user-state @user) "[initial-user-state] not restored after logout")
        (is (= initial-token-state @tokens) "[initial-token-state] not restored after logout")
        (is (= {} @ls) "Local storage was not reset after logout")))))

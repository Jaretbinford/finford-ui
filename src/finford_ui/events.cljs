(ns finford-ui.events
  (:require
    ["dayjs" :as dayjs]
    [clojure.string :as string]
    [re-frame.core :as rf]
    [lambdaisland.glogi :as log]
    [finford-ui.utils.helpers :refer [>dis] :as helpers]
    [finford-ui.config :refer [cognito-config] :as config]
    [finford-ui.interceptors :refer [standard-interceptors debug-chan]]
    [finford-ui.db :as db]))


; region = Register CoFx ==============================================================

; Usage: When registering a event (rf/inject-cofx ::uuid) as an interceptor
; Description: A cofx to inject a uuid into the event.
(rf/reg-cofx
  ::uuid
  (fn [cofx _]
    (assoc cofx :uuid (str (random-uuid)))))

; Usage: When registering a event (rf/inject-cofx ::timestamp) as an interceptor
; Description: A cofx to inject a timestamp represented as a string into the event.
;              Format of `Mon May 07 2018 08:13:14 GMT-0500`
(rf/reg-cofx
  ::timestamp
  (fn [cofx _]
      (assoc cofx :timestamp (.toString (dayjs)))))

; Usage: When registering a event (rf/inject-cofx ::now) as an interceptor
; Description: A cofx to inject a dayjs object of the current time into the event.
(rf/reg-cofx
  ::now
  (fn [cofx _]
    (assoc cofx :now (dayjs))))

; Usage: When registering a event (rf/inject-cofx ::refresh-offset) as an interceptor
; Description: A cofx to inject the value of token-refresh-offset-min into the event.
(rf/reg-cofx
  ::refresh-offset
  (fn [cofx _]
    (assoc cofx :offset config/token-refresh-offset-min)))

; Usage: When registering an event (rf/inject-cofx ::url-hash) as an interceptor
; Description: Retrieve the window.location.hash from the current url and inject it into the event
(rf/reg-cofx
  ::url-hash
  (fn [cofx _]
    (assoc cofx :url-hash (.-hash js/window.location))))

; Usage: When registering an event (rf/inject-cofx ::user-pool) as an interceptor
; Description: Create and inject a cognito user-pool into the event.
(rf/reg-cofx
  ::user-pool
  (fn [cofx _]
    (assoc cofx :user-pool (new js/AmazonCognitoIdentity.CognitoUserPool (clj->js {:UserPoolId (:user-pool cognito-config)
                                                                                   :ClientId (:user-pool-client cognito-config)})))))

; Usage: When registering an event (rf/inject-cofx ::unauthenticated-cognito-user) as an interceptor
; Description: Create and inject a cognito user into the event.
;              This is used in the following locations:
;              1) Login: In that case we will have the username in [:db :routes :login :username]
;              2) Refreshing Tokens: In which case we will have [:db :user :username]
;              3) Recovering Password: In which case we will have [:db :routes :lost-pass :lost-pass-request-form :username :value]
(rf/reg-cofx
  ::unauthenticated-cognito-user
  (fn [cofx _]
    (let [username (or (-> cofx :db :user :username)
                       (-> cofx :db :routes :login :login-form :username :value)
                       (-> cofx :db :routes :lost-pass :lost-pass-request-form :username :value))]
      (if username
        (assoc cofx :unauthenticated-cognito-user (helpers/new-cognito-user username))
        cofx))))

(rf/reg-cofx
  ::cognito-auth-details
  (fn [cofx _]
    (let [username (-> cofx :db :routes :login :login-form :username :value)
          password (-> cofx :db :routes :login :login-form :password :value)]
      (assoc cofx :cognito-auth-details (new js/AmazonCognitoIdentity.AuthenticationDetails (clj->js {:Username username :Password password}))))))

;; endregion

; region = Register Effect Handlers ===================================================

; Usage: In the returned map of a reg-event-fx {:save-in-local-storage {:key1 value1 :key2 value2}}
; Description: Saves values form a map into localStorage
(rf/reg-fx
  ::save-in-local-storage
  (fn [map-to-store]
    (doseq [[k v] map-to-store]
      (.setItem js/localStorage (name k) v))))

; Usage: In the returned map of a reg-event-fx {::remove-from-local-storage [:user :username]}
; Description: Removes desired values from localStorage
(rf/reg-fx
  ::remove-from-local-storage
  (fn [keys]
    (doseq [k keys]
      (.removeItem js/localStorage (name k)))))

; Usage: In the returned map of a reg-event-fx {:update-url-hash "/some-route?optional=args"}
; Description: Update the window location to the hash route matching the provided arg
;              This will run through the router and be added to history
(rf/reg-fx
  ::update-url-hash
  (fn [new-hash-route]
    (set! (.-hash js/window.location) new-hash-route)))

(rf/reg-fx
  ::establish-debug-channel
  (fn [[debug-override?]]
    (let [uri (subs (.-hash js/window.location) 1)
          turn-on? (or config/app-debugger-active? (= "true" debug-override?))]
      (when (and turn-on? (not= "/debug" uri))
        (let [channel (js/BroadcastChannel. "debugChan")]
          (log/info :message "Debug Broadcast Channel Created")
          (swap! debug-chan assoc :chan channel))))))
;; endregion

; region = Register Events ============================================================

; Usage: [::noop]
; Description: Does nothing. Used for http success/errors that do not matter
(rf/reg-event-fx
  ::noop
  [standard-interceptors]
  (fn [cofx _]
    (.log js/console "Noop")
    {}))

; Usage: [::set-url-hash "/some-route?optional=arg"]
; Description: Set url hash. This will run through the router and have proper history updates
(rf/reg-event-fx
  ::set-url-hash
  [standard-interceptors]
  (fn [_ [_ hash-url]]
    {::update-url-hash hash-url}))

; region -- App Start Up --------------------------------------------------------------
(defn get-from-ls
  "Attempt to retrieve ls-key from localStorage. If value is equal to 'null'
   return nil"
  [ls-key]
  (let [value (.getItem js/localStorage ls-key)]
    (if (= "null" value) nil value)))

; Usage: When registering an event (rf/inject-cofx ::local-store) as an interceptor
; Description: Retrieve the existing localStorage information and inject it into the event. Value will be nil if key is not present
(rf/reg-cofx
  ::init-local-store
  (fn [cofx _]
    (-> cofx
        (assoc :debug-override? (get-from-ls "finford-ui-debug-override"))
        (assoc :ls-tokens {:id (get-from-ls "finford-ui-cognito-id-token")
                           :refresh (get-from-ls "finford-ui-cognito-refresh-token")})
        (assoc :ls-user {:uuid (get-from-ls "finford-ui-uuid")
                         :permissions (get-from-ls "finford-ui-permissions")
                         :email (get-from-ls "finford-ui-email")
                         :username (get-from-ls "finford-ui-username")}))))

; Usage: (rf/dispatch [::events/initialize-db])
; Description: Establish initial application state of app-db
(rf/reg-event-fx
  ::initialize-db
  [standard-interceptors
   (rf/inject-cofx ::init-local-store)]
  (fn [{:keys [ls-tokens ls-user debug-override?]} event]
    {:db (assoc db/default-db :user ls-user
                              :tokens {:refresh (:refresh ls-tokens)
                                       :id (:id ls-tokens)})
     ::establish-debug-channel [debug-override?]}))
;; endregion

; region -- Alerts --------------------------------------------------------------------

; Usage: ::add-alert and ::remove-alert are typically used at the same time.
;        In a reg-event-fx like so:
;        {:dispatch       [::add-alert uuid "Alert body here" "Alert Type ie: danger, info, etc"]
;         :dispatch-later [{:ms config/alert-timeout-ms
;                           :dispatch [::remove-alert uuid]}]}
; Description: Extends the top level `:alerts` vector with a alert map {:uuid #uuid :type "..." :message "..."}
(rf/reg-event-db
  ::add-alert
  [standard-interceptors]
  (fn [db [_ uuid message alert-type]]
    (update db :alerts merge {:uuid uuid :message message :type alert-type})))

; Description: Removes the alert with the matching uuid from the top-level
;              `:alerts` vector. See above for additional detail
(rf/reg-event-db
  ::remove-alert
  [standard-interceptors]
  (fn [db [_ uuid]]
    (update db :alerts helpers/remove-matching-uuid uuid)))
;; endregion

; region -- Routing -------------------------------------------------------------------
(defn update-events-with-actual-params
  "Events defined in the router to be dispatched when a route is navigated to
   use `:query-params` and `:path-params` as place holders for the actual values
   defined later."
  [events path query]
  (reduce (fn [res event]
            (conj res (mapv (fn [el]
                              (case el
                                :query-params query
                                :path-params path
                                el))
                            event)))
          []
          events))

(rf/reg-event-fx
  ::dispatch-route-events
  [standard-interceptors]
  (fn [_ [_ {:keys [path-params query-params] :as active-route}]]
    (let [route-initialization-events (get-in active-route [:data :dispatch-on-entry] [])]
      {:dispatch-n (update-events-with-actual-params route-initialization-events path-params query-params)})))

; Usage: (dispatch [::shared-events/set-active-route (rfront/match-by-path router/routes uri)])
; Description: Change the active reitit route for the app
(rf/reg-event-fx
  ::set-active-route
  [standard-interceptors]
  (fn [{:keys [db]} [_ {:keys [path-params query-params] :as route}]]
    {:db (-> db
             (assoc :active-route route)
             (assoc :path-params path-params)
             (assoc :query-params query-params))}))

; Usage: (rf/dispatch [::authenticated-route-login-required])
; Description: Captures the url has of the desired authenticated route and redirects to /login
(rf/reg-event-fx
  ::authenticated-route-login-required
  [standard-interceptors
   (rf/inject-cofx ::url-hash)]
  (fn [{db :db url-hash :url-hash} _]
    (let [auth-route (subs url-hash 1)]
      {:db (assoc db :requested-auth-route (if (= auth-route "/login") "/" auth-route))
      ::update-url-hash "/login"})))

;; endregion

; region -- AuthN ---------------------------------------------------------------------
(rf/reg-event-fx
  ::set-tokens
  [standard-interceptors
   (rf/inject-cofx ::refresh-offset)
   (rf/inject-cofx ::now)]
  (fn [{:keys [db offset now]} [_ {:keys [id refresh]}]]
    (let [{:keys [email cognito:username exp]} (helpers/decode-token id)]
      {:db (-> db
               (assoc-in [:user :email] email)
               (assoc-in [:user :username] cognito:username)
               (assoc-in [:tokens :id] id)
               (assoc-in [:tokens :refresh] refresh))
       :dispatch-later [{:ms (helpers/ms-until-refresh exp now offset)
                         :dispatch [::request-fresh-tokens]}]
       ::save-in-local-storage {:finford-ui-cognito-id-token id
                                :finford-ui-cognito-refresh-token refresh
                                :finford-ui-email email
                                :finford-ui-username cognito:username}})))

(rf/reg-fx
  ::refresh-session
  (fn [[^js cognito-user token uuid]]
    (.refreshSession cognito-user token (fn [error ^js response]
                                          (if error
                                            (>dis [::add-alert uuid (-> error (js->clj :keywordize-keys true) :message) "danger"])
                                            (do
                                              (.log js/console "Auth token successfully refreshed as " (.toString (dayjs)))
                                              (>dis [::set-tokens {:id (-> response .getIdToken .getJwtToken)
                                                                   :refresh (-> response .getRefreshToken .getToken)}])))))))

; This is called in a perpetual loop by a dispatch-later in `::set-tokens`
; Once a user logs out we want to terminate the loop, by not calling `::refresh-session` again.
(rf/reg-event-fx
  ::request-fresh-tokens
  [(rf/inject-cofx ::uuid)
   (rf/inject-cofx ::unauthenticated-cognito-user)]
  (fn [{:keys [db uuid unauthenticated-cognito-user]} _]
    (if (-> db :user :username) ;; The user has not logged out
      ;  This token is goofy but when calling aws-sdk functions *looking at you refresh*
      ;  They want an object that has a `getToken()` method that gives them the token
      ;  instead of the actual token for a argument. So the dude abides."
      (let [token (clj->js {:getToken (fn [] (-> db :tokens :refresh))})]
        {::refresh-session [unauthenticated-cognito-user token uuid]})
      {})))

(rf/reg-fx
  ::cognito-signout
  (fn [^js cognito-user]
    ;; Or `.globalSignOut(callback)`
    (when cognito-user (.signOut cognito-user))))

(rf/reg-event-fx
  ::logout
  [standard-interceptors]
  (fn [{:keys [db]} _]
    (let [cognito-user (-> db :user :authenticated-cognito-user)]
      {:db (-> db
               (assoc-in [:user :email] nil)
               (assoc-in [:user :username] nil)
               (assoc-in [:user :authenticated-cognito-user] nil)
               (assoc-in [:tokens :id] nil)
               (assoc-in [:tokens :refresh] nil))
       ::remove-from-local-storage [:finford-ui-cognito-id-token
                                    :finford-ui-cognito-refresh-token
                                    :finford-ui-email
                                    :finford-ui-username]
       ::cognito-signout cognito-user})))

;; endregion

; region -- Form Validation ----------------------------------------------------

; This method should be extended as needed for any custom field validation.
; Recommended naming convention is `(defmethod shared-events/input-error? :<FORM-NAME>/<FIELD-NAME> [m] (bool ...))` The
; function will receive a map with the following shape to use in determining if
; the field is valid: {:db ..
;                      :field-name ..
;                      :form ..
;                      :validator-fn ..
;                      :value ..
;                      :dirty? ..
;                      :required? ..
;                      :form-name ..}
; Return bool indicating if there is an error.
(defmulti input-error? (fn [{:keys [validator-fn]}]
                              validator-fn))

; Usage: [::set-form-field-value :form-name :field-name "new value"]
; Description: Sets the value of the provided field in the form. The field is
;              considered dirty once this happens and it's error status will be updated.
(rf/reg-event-db
  ::set-form-field-value
  [standard-interceptors]
  (fn [db [_ route-name form-name field-name value]]
    (let [form-path [:routes route-name form-name]
          form (get-in db form-path)
          {:keys [dirty? required? validator-fn]} (field-name form)
          has-error? (input-error? {:db db
                                    :validator-fn validator-fn
                                    :field-name field-name
                                    :form form
                                    :value value
                                    :dirty? dirty?
                                    :required? required?
                                    :form-name form-name})]
      (-> db
          (assoc-in (into form-path [field-name :value]) value)
          (assoc-in (into form-path [field-name :has-error?]) has-error?)))))

; Usage: [::reset-and-clear-form-field-values :form-name :field-name {:field-name "Default Value"}]
; Description: Sets the value of :dirty? to false and Lvalue to nil for all form fields. Unless
;              default-vals are provided for a specific field name which will be used instead of nil.
(rf/reg-event-db
  ::reset-and-clear-form-field-values
  [standard-interceptors]
  (fn [db [_ route-name form-name default-vals]]
    (let [form (-> db :routes route-name form-name)
          reset-form (reduce (fn [res [k v]]
                               (let [new-value (if (contains? default-vals k)
                                                 (assoc v :dirty? false :value (k default-vals))
                                                 (assoc v :dirty? false :value nil))]
                                 (merge res {k new-value}))) {} form)]
      (assoc-in db [:routes route-name form-name] reset-form))))

; Usage: [::set-form-field-dirty :form-name :field-name]
; Description: Sets the value of :dirty? to true for the provided field in the form.
(rf/reg-event-db
  ::set-form-field-dirty
  [standard-interceptors]
  (fn [db [_ route-name form-name field-name]]
    (let [form-path [:routes route-name form-name]
          form (get-in db form-path)
          value (-> form field-name :value)
          required? (-> form field-name :required?)
          validator-fn (-> form field-name :validator-fn)
          has-error? (input-error? {:db db
                                    :validator-fn validator-fn
                                    :field-name field-name
                                    :form form
                                    :value value
                                    :dirty? true
                                    :required? required?
                                    :form-name form-name})]
      (-> db
          (assoc-in (into form-path [field-name :dirty?]) true)
          (assoc-in (into form-path [field-name :has-error?]) has-error?)))))

(defn invalidator-reducer
  "Reducing function used to build a new form map when invalidating a form prior to submission.
  Marks fields as dirty and having an error if they begin as both required and clean"
  [res [k v]]
  (let [clean-and-required? (and (not (:dirty? v)) (:required? v))
        new-value (if clean-and-required?
                    (assoc v
                      :dirty? true
                      :has-error? (input-error? {:db (:db res)
                                                 :field-name k
                                                 :validator-fn (:validator-fn v)
                                                 :form (:form res)
                                                 :value (:value v)
                                                 :required? (:required? v)
                                                 :dirty? true
                                                 :form-name (:form-name res)}))
                    v)]
    (merge res {k new-value})))

; Usage: [::submit-if-form-valid :route-name :form-name [:event-to-dispatch]]
; Description: Checks to see if the named form is valid. A valid form is considered
;              to be one that does not have any known errors or required fields which are
;              not dirty. If form is in a valid state. Events are dispatched.
(rf/reg-event-fx
  ::submit-if-form-valid
  [standard-interceptors]
  (fn [{:keys [db]} [_ route-name form-name & events]]
    (let [form-path [:routes route-name form-name]
          form (get-in db form-path)
          invalidated-form (-> (reduce invalidator-reducer {:db db :form-name form-name :form form} form)
                               (dissoc :form-name :form :db))
          required-fields (filter :required? (vals invalidated-form))
          form-valid? (every? #(and (:dirty? %) (not (:has-error? %))) required-fields)]
      (merge {:db (assoc-in db form-path invalidated-form)}
             (when form-valid? {:dispatch-n (into [] events)})))))
; endregion
;; endregion


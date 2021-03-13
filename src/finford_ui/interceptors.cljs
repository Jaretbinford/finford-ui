(ns finford-ui.interceptors
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [reagent.core :as r]
    [reitit.core :refer [Match]]
    [re-frame.core :as rf]
    [expound.alpha :as expound]
    [cognitect.transit :as t]
    [clojure.walk :refer [postwalk]]
    [re-frame.core :as rf]
    [finford-ui.db :as db]
    [finford-ui.config :as config]))

;; The rationale for this being in an atom is if the db gets wiped out debug info will still
;; be able to be broadcast.
(defonce debug-chan (r/atom {}))

(defn check-and-throw
  "Every time we update the db check if the spec is valid if not throw exception"
  [db]
  (when-not (s/valid? ::db/db db)
    (throw (ex-info (str "spec check failed: " (expound/expound ::db/db db)) {}))))

(defn make-suitable-to-post [db]
  (postwalk (fn [n]
              (cond
                (= Match (type n)) (dissoc n :data)
                (fn? n) "This is a JS fn"
                :else n)) db))

(def debugger
  (rf/->interceptor
    :id    :debugger
    :after (fn [{:keys [effects] {:keys [event db]} :coeffects :as context}]
             (when-let [channel (:chan @debug-chan)]
               (let [effect-with-js-obj? (contains? #{:finford-ui.routes.auth.login.events/successful-login} (first event))
                     db (make-suitable-to-post (-> (or (:db effects) db)
                                                   (assoc-in [:user :authenticated-cognito-user] "Stupid Cognito JS Nonsense")))
                     event (cond
                             effect-with-js-obj? [(first event) "JS Objects Here"]
                             :else (make-suitable-to-post event))
                     w (t/writer :json)]
                 (.postMessage channel (t/write w {:event event
                                                   :db db}))))
             context)))

;; = Additional Interceptors Can Be Added To The Vector As Needed
(def standard-interceptors [debugger
                            ;; Turn on for shitty debugging situations
                            ; (when ^boolean goog.DEBUG rf/debug)
                            ;; Spec check the db this can be agressive, but useful
                            ; (when ^boolean goog.DEBUG (rf/after check-and-throw))
                            ])

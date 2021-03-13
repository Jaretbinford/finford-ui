(ns finford-ui.db
  (:require
    [clojure.spec.alpha :as s]
    [finford-ui.routes.auth.login.db :as login]
    [finford-ui.routes.auth.lost-pass.db :as lost-pass]
    [finford-ui.routes.auth.change-pass.db :as change-pass]
    [finford-ui.routes.auth.register.db :as register]
    [finford-ui.utils.helpers :refer [valid-uuid-str? uuid-str-gen valid-jwt? email-gen]]
    [finford-ui.routes.index.db :as index]
    [finford-ui.routes.debug.db :as debug]))

; 
; 

;; = Specs =====================================================================
(s/def ::uuid
  (s/with-gen
    (s/nilable valid-uuid-str?)
    #(uuid-str-gen)))
(s/def ::map-of-keywords-and-strs (s/every-kv keyword string?))

;; - Tokens --------------------------------------------------------------------
(s/def ::token valid-jwt?)
(s/def ::id (s/nilable ::token))
(s/def ::refresh (s/nilable string?)) ;; {"cty":"JWT","enc":"A256GCM","alg":"RSA-OAEP"}
(s/def ::tokens (s/keys :req-un [::id ::refresh]))

;; -- User ---------------------------------------------------------------------
(s/def ::username (s/nilable string?))
(s/def ::password (s/nilable string?))
(s/def ::permissions (s/nilable (s/coll-of string?)))
(s/def ::email
  (s/nilable (s/with-gen
               #(re-matches #".+@.+\..+" %)
               (fn [] email-gen))))
(s/def ::user (s/keys :req-un [::uuid ::permissions ::email ::username]))

;; - Active Route --------------------------------------------------------------
(s/def ::name keyword?)
(s/def ::view vector?)
(s/def ::dispatch-on-entry vector?)
(s/def ::path-params ::map-of-keywords-and-strs)
(s/def ::query-params ::map-of-keywords-and-strs)
(s/def ::data (s/keys :req-un [::name ::view]
                      :opt-un [::dispatch-on-entry]))
(s/def ::active-route (s/keys :req-un [::data ::path-params ::query-params]))

;; -- Alerts -------------------------------------------------------------------
(s/def ::type string?)
(s/def ::message string?)
(s/def ::alert (s/keys :req-un [::uuid ::message ::type]))
(s/def ::alerts (s/* ::alert))

;; -- Route DBs ----------------------------------------------------------------
(s/def ::index ::index/index-db)
(s/def ::debug ::debug/debug-db)
(s/def ::login ::login/login-db)
(s/def ::lost-pass ::lost-pass/lost-pass-db)
(s/def ::register ::register/register-db)
(s/def ::routes (s/keys :req-un [::index
                                 ::debug
                                 ::register
                                 ::login
                                 ::lost-pass
                                 ::change-pass]))

;; = DB Spec ===================================================================
(s/def ::db (s/keys :req-un [::tokens
                             ::user
                             ::routes
                             ::active-route
                             ::requested-auth-route
                             ::alerts]))

(defonce default-db {;; = Top Level ============================================
                     :tokens {:id nil :refresh nil}
                     :user {:uuid nil :permissions nil :email nil :username nil :authenticated-cognito-user nil}
                     :active-route {:path-params {}
                                    :query-params {}
                                    :data {:name :loading
                                           :view [:div "Loading..."]}}
                     :requested-auth-route nil
                     :alerts '()
                     ;; = Routes ===============================================
                     :routes {:index index/index-db
                              :debug debug/debug-db
                              :register register/register-db
                              :login login/login-db
                              :lost-pass lost-pass/lost-pass-db
                              :change-pass change-pass/change-pass-db}})

; 
; 

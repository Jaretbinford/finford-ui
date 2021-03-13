(ns finford-ui.routes.index.db
  (:require [clojure.spec.alpha :as s]))

;; = Functions =================================================================
(defn valid-hex-color? [hex]
  (re-matches #"\#[A-F0-9]{0,6}" hex))

;; = Specs =====================================================================
(s/def ::hex-color valid-hex-color?)
(s/def ::index-db (s/keys :req-un [::hex-color]))

;; = Default Index Route DB Map ================================================
(defonce index-db {:hex-color "#3E3E3E"})

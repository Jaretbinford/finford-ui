(ns finford-ui.routes.debug.db
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::debug-db (s/keys :req-un []))
(defonce debug-db {:show-diff? false})

(ns finford-ui.index-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [finford-ui.test-utils :as test-utils]
            [finford-ui.events :as shared-events]
            [finford-ui.routes.index.events :as sut-events]
            [finford-ui.routes.index.subs :as sut-subs]
            [goog.string :as gstring]
            [re-frame.core :as rf]))

(defn setup-teardown [f]
  (test-utils/stub-local-store)
  (f))

(use-fixtures :each setup-teardown)

(deftest getting-and-setting-hex-color
  (rf-test/run-test-sync
    (rf/dispatch [::shared-events/initialize-db])
    (let [color (rf/subscribe [::sut-subs/hex-color])
          spec-error "spec check failed:"]
      (is (= "#3E3E3E" @color))
      (rf/dispatch [::sut-events/set-color "#3E3E3"])
      (is (= "#3E3E3" @color))
      (try (rf/dispatch [::sut-events/set-color "#3E3E3XX"])
           (catch js/Error e (is (gstring/startsWith (.-message e) spec-error)))))))

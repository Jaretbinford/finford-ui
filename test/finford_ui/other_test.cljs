(ns finford-ui.other-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [finford-ui.test-utils :as test-utils]
            [finford-ui.events :as shared-events]
            [finford-ui.subs :as shared-subs]
            [finford-ui.routes.other.events :as sut-events]
            [re-frame.core :as rf]))

(use-fixtures :each {:before (test-utils/stub-local-store)})

(deftest properly-display-an-alert-when-button-is-pressed
  (rf-test/run-test-async
    (rf/dispatch-sync [::shared-events/initialize-db])
    (let [alerts (rf/subscribe [::shared-subs/alerts])]
      (is (empty? @alerts))
      (rf/dispatch [::sut-events/sound-alert])
      (rf-test/wait-for [::shared-events/add-alert]
                        (is (not-empty @alerts))
                        (rf-test/wait-for [::shared-events/remove-alert]
                                          (is (empty? @alerts)))))))

(ns mlc.events
    (:require [clojure.set :refer [union difference]]
              [re-frame.core :as rf]
              [mlc.db :as db]))

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-db
  :filter-checkbox-click
  (fn [db [_ name checked]]
    (js/console.log (clj->js db))
    (if checked
      (assoc db :active-groups (union (:active-groups db) #{name}))
      (assoc db :active-groups (difference (:active-groups db) #{name})))))

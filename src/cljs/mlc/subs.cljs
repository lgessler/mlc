(ns mlc.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [clojure.set :refer [intersection union]]
            [re-frame.core :as re-frame]))

;; -- Helpers -----------------------------------------------------------------

(defn- filter-by-active-groups
  [db]
  (let [active-groups (:active-groups db)]
    (filter 
      (fn [pt]
        (-> (intersection (:language-groups pt) (:active-groups db)) 
            empty?
            not))
      (:points db))))

(defn- filter-by-query
  [db]
  (:points db))

;; -- Subscriptions -----------------------------------------------------------
(re-frame/reg-sub
  :loaded
  (fn [db]
    (:loaded db)))

(re-frame/reg-sub
  :filtered-points
  (fn [db]
    (if (= (:query db) "")
      (filter-by-active-groups db)
      (filter-by-query db))))

(re-frame/reg-sub
  :group-is-empty
  (fn [db [_ group-name]]
    (not (some #{group-name} (reduce union (map :language-groups (:points db)))))))

(re-frame/reg-sub
  :point-from-latlng
  (fn [db [_ lat lng]]
    (first (filter #(and (= (:lat %) lat) 
                         (= (:lng %) lng)) 
                   (:points db)))))

(re-frame/reg-sub
  :current-point
  (fn [db _]
    (:current-point db)))

(re-frame/reg-sub
  :query
  (fn [db _]
    (:query db)))

(re-frame/reg-sub
  :query-is-nonempty
  (fn [db _]
    (not (= (:query db) ""))))

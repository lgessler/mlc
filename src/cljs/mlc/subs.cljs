(ns mlc.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [clojure.set :refer [intersection union]]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :filtered-points
  (fn [db]
    (let [active-groups (:active-groups db)]
      (filter 
        (fn [pt]
          (-> (intersection (:language-groups pt) (:active-groups db)) 
              empty?
              not))
        (:points db)))))

(re-frame/reg-sub
  :empty-group?
  (fn [db [_ group-name]]
    (not 
      (contains?
        (reduce union (map :language-groups (:points db)))
        group-name))))

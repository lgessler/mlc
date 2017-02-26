(ns mlc.events
    (:require [clojure.set :refer [union difference]]
              [cljs.spec :as s]
              [re-frame.core :as rf]
              [ajax.core :as ajax]
              [mlc.db :as mlcdb]))

;; -- Constants ---------------------------------------------------------------
(def sheet-url (str "https://docs.google.com/spreadsheets/d/"
                    "112wUDcvKnJLnVkAwm0rM7CJy9MWVKmE-t5EbziHr4vc/pubhtml"))
(def selected-icon-path "img/marker-icon-red.png")
(def unselected-icon-path "img/marker-icon-blue.png")

;; -- Interceptors ------------------------------------------------------------
(defn check-and-throw
  "throw an exception if the db doesn't match the spec"
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :mlc.db/db)))
(def default-intrs [check-spec-interceptor])

;; -- Helpers -----------------------------------------------------------------

;; -- Events ------------------------------------------------------------------
(rf/reg-event-fx
  :initialize-db
  (fn [_ _]
    {:db {:loaded false}
     :tabletop sheet-url}))

(rf/reg-event-db
  :load-sheets-data
  default-intrs
  (fn [db [_ data]]
    (let [clj-data (js->clj data)
          keywordify-and-parse (fn [[k v]] 
                                 (cond (#{"lat" "lng"} k) 
                                       [(keyword k) 
                                        (js/parseFloat v)]
                                       (#{"language-groups" "languages"} k)
                                       [(keyword k) 
                                        (into #{} (clojure.string/split v #",\s?"))]
                                       :else [(keyword k) v]))
          points (map #(into {} (map keywordify-and-parse %)) clj-data)]
      {:points points
       :markers nil
       :active-groups mlcdb/language-group-names
       :current-point nil
       :query ""
       :loaded true})))

(rf/reg-event-db
  :filter-checkbox-click
  default-intrs
  (fn [db [_ name checked]]
    (if checked
      (assoc db :active-groups (union (:active-groups db) #{name}))
      (assoc db :active-groups (difference (:active-groups db) #{name})))))

(rf/reg-event-fx
  :marker-clicked
  default-intrs
  (fn [fx [_ point evt]]
    (let [db (:db fx)]
      {:db (assoc db :current-point point)
       :leaflet {:old-point (:current-point db)
                 :new-point point
                 :markers (:markers db)}})))

(rf/reg-event-db
  :update-query
  default-intrs
  (fn [db [_ new-query]]
    (assoc db :query new-query)))


(rf/reg-event-db
  :new-markers
  default-intrs
  (fn [db [_ new-markers]]
    (assoc db :markers new-markers)))


;; -- Effects -----------------------------------------------------------------
(rf/reg-fx
  :tabletop
  (fn [sheet-url]
    (js/Tabletop.init #js {:key sheet-url
                           :simpleSheet true
                           ;; tabletop doesn't really have affordances for
                           ;; error handling, so assume all went well...
                           :callback (fn [data tabletop] 
                                       (rf/dispatch [:load-sheets-data data]))})))

(defn- same-latlng
  [point marker]
  (and (= (:lat point) (.-lat (aget marker "_latlng")))
       (= (:lng point) (.-lng (aget marker "_latlng")))))

(defn- update-icon!
  [marker icon-path]
  (aset (aget marker "_icon") "src" icon-path))

;; to do: 
;; 1. find markers that correspond to points
;; 2. adjust colors
(rf/reg-fx
  :leaflet
  (fn [{:keys [new-point old-point markers]}]
    (let [new-marker (first (filter (partial same-latlng new-point) markers))
          old-marker (first (filter (partial same-latlng old-point) markers))]

      (when-not (nil? old-point)
        (update-icon! old-marker unselected-icon-path))
      (update-icon! new-marker selected-icon-path))))


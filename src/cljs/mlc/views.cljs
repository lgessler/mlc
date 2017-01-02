(ns mlc.views
  (:require [clojure.set :refer [union difference]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [mlc.db :refer [language-group-names]]))

;;------------------------------------
;; Sidebar
;;------------------------------------
(defn sidebar-row
  [name]
  [:div.item 
   [:p.description name]
   [:p.item-value "yo"]])

(defn sidebar [] 
  (let [sidebar-items ["Name" "Languages" "Number of Speakers" "Type" "Address"]]
    [:div#sidebar.col-sm-4.col-md-3 
     [:img#street-view]
     [:div#sidebar-strip] ;; color
     (for [iname sidebar-items]
       ^{:key iname} [sidebar-row iname])]))

;;------------------------------------
;; Filter Menu
;;------------------------------------
(defn handle-filter-click [a _ _]
  (let [target (-> a .-target)
        name (.-value target)
        checked (.-checked target)]
    (rf/dispatch [:filter-checkbox-click name checked])))

(defn filter-row [name]
  (let [id (str "checkbox-" name)
        is-empty (rf/subscribe [:empty-group? name])]
    [:div
     [:input {:type "checkbox" 
              :value name 
              :id id 
              :disabled @is-empty
              :defaultChecked (not @is-empty)
              :on-click handle-filter-click}]
     [:label {:for id} name]]))

(defn filter-menu []
  [:div#filter-menu
   [:form
    (for [n language-group-names]
      ^{:key n} [filter-row n])]])

;;------------------------------------
;; Leaflet map container
;;------------------------------------
(defn leaflet-inner []
  (let [leaflet (atom nil)
        starting-point [43.0755408 -89.4089052]
        zoom 13
        southWestBound (.latLng js/L 42.9720177 -89.5872175)
        northEastBound (.latLng js/L 43.2317151 -89.150511)
        bounds (.latLngBounds js/L southWestBound northEastBound)
        tiles (.tileLayer js/L 
                          "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
                          (clj->js {:attributionControl false 
                                    :maxZoom 16 
                                    :minZoom 12}))
        ;; to update, just             
        do-update (fn [comp]
                 (let [map (:map @leaflet)
                       markers (:markers @leaflet)
                       points (:points (reagent/props comp))]
                   
                   (doseq [marker markers]
                     (.removeLayer map marker))

                   (let [new-markers (loop [points points v (transient [])]
                                       (if (empty? points)
                                         (persistent! v)
                                         (let [pt (first points)]
                                           (do (conj! v (.addTo 
                                                          (.marker js/L 
                                                                   (clj->js [(:lat pt) (:lng pt)]))
                                                          map));(.on "click" handle-click)
                                               (recur (rest points) v)))))]
                     (swap! leaflet assoc :markers new-markers))))
        init (fn [comp]
               (let [map (.map js/L "map")]
                 ;; move to starting point
                 (.setView map (clj->js starting-point) zoom)

                 ;; set max bounds to prevent scrolling outside of madison
                 (.setMaxBounds map bounds)

                 ;; add tile layer to map
                 (.addTo tiles map)

                 ;; hide attribution
                 (-> (js/$ ".leaflet-control-attribution")
                     .hide)

                 ;; from https://github.com/Leaflet/Leaflet/issues/1266
                 ;; who knows what this means...
                 (.trigger 
                   (.on (js/$ js/window) 
                        "resize" 
                        (fn [] 
                          (.height (js/$ "#map") (.height (js/$ js/window)))
                          (.invalidateSize map)))
                   "resize")
                

                 (reset! leaflet {:map map :markers []})
                 (do-update comp)))]

    (reagent/create-class
      {:reagent-render (fn []
                         [:div#map-container.col-sm-8.offset-sm-4.col-md-9.offset-md-3.main
                          [:div#map]])
       :component-did-mount init
       :component-did-update do-update
       :display-name "leaflet-inner"})))

;(let [lmap (.setView (.map js/L "map") #js [43.0755408 -89.4089052] 13)

(defn map-container []
  (let [points (rf/subscribe [:filtered-points])]
    (fn []
      [leaflet-inner {:points @points}])))

;;------------------------------------
;; Put the right letters together and make a better day
;;------------------------------------
(defn main-panel []
  (fn []
    [:div
     [:div.row.row-eq-height
      [sidebar]
      [map-container]]
     [filter-menu]]))

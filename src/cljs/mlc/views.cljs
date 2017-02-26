(ns mlc.views
  (:require [clojure.set :refer [union difference]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [mlc.db :refer [language-group-names]]))

;; -- Sidebar -----------------------------------------------------------------
(defn sidebar-row
  [iname ival]
  [:div.item 
   [:p.description iname]
   [:p.item-value ival]])

(defn street-view-url 
  [address]
  (str "https://maps.googleapis.com/maps/api/streetview?size=400x200&location=" 
      address 
      "&key="
      "AIzaSyDmt7eu9luPMK3H8jYJbK9Tjz081l3BT-A"))

(defn sidebar [] 
  (reagent/create-class
    {:reagent-render 
     (fn [props]
       (let [sidebar-items {:name "Name" 
                            :languages "Languages" 
                            :number-of-speakers "Number of Speakers" 
                            :type "Type" 
                            :address "Address"}
             current-point (:current-point props)]
         [:div#sidebar.col-sm-4.col-md-3 
          (if current-point
            [:div
             [:img.street-view {:src (street-view-url (:address current-point))
                                :on-error (fn [e & a] (js/console.log e a))}]
             [:div#sidebar-strip]
             (for [[kw iname] (seq sidebar-items)]
               (let [ival (kw current-point)
                     ival (if (= kw :languages)
                            (clojure.string/join ", " ival)
                            ival)]
                 ^{:key iname} [sidebar-row iname ival]))]
            [:div.item {:style {:padding "30px"}} [:p [:em "Click a marker"]]])]))}))

;; -- Leaflet map container ---------------------------------------------------
(defn leaflet-inner []
  (let [leaflet (atom nil)
        starting-point [43.0755408 -89.4089052]
        zoom 13
        southwest-bound (.latLng js/L 42.9720177 -89.5872175)
        northeast-bound (.latLng js/L 43.2317151 -89.150511)
        bounds (.latLngBounds js/L southwest-bound northeast-bound)
        tiles (.tileLayer js/L 
                          "http://{s}.tile.osm.org/{z}/{x}/{y}.png"
                          (clj->js {:attributionControl false 
                                    :maxZoom 16 
                                    :minZoom 12}))
        marker-clicked (fn [a _ _]
                         (let [target (-> a .-target)
                               lat (-> target .getLatLng .-lat)
                               lng (-> target .getLatLng .-lng)
                               point (rf/subscribe [:point-from-latlng lat lng])]
                           (rf/dispatch [:marker-clicked @point a])))
        do-update 
        (fn [comp]
          (let [map (:map @leaflet)
                markers (:markers @leaflet)
                points (:points (reagent/props comp))]

            (doseq [marker markers]
              (.removeLayer map marker))

            (let [new-markers (loop [points points v (transient [])]
                                (if (empty? points)
                                  (persistent! v)
                                  (let [pt (first points)]
                                    (do (conj! v (-> (.marker js/L 
                                                              (clj->js [(:lat pt) (:lng pt)]))
                                                     (.addTo map)
                                                     (.on "click" marker-clicked)))
                                        (recur (rest points) v)))))]
              (js/console.log (first new-markers))
              (rf/dispatch [:new-markers new-markers])
              (swap! leaflet assoc :markers new-markers))))
        init 
        (fn [comp]
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

(defn map-container []
  (let [points (rf/subscribe [:filtered-points])]
    (fn []
      [leaflet-inner {:points @points}])))

;; -- Filter Menu -------------------------------------------------------------
(defn handle-filter-click [a _ _]
  (let [target (.-target a)
        name (.-value target)
        checked (.-checked target)]
    (rf/dispatch [:filter-checkbox-click name checked])))

(defn filter-row [name]
  (let [id (str "checkbox-" name)
        group-is-empty (rf/subscribe [:group-is-empty name])
        query-is-nonempty (rf/subscribe [:query-is-nonempty])]
    [:div
     [:input {:type "checkbox" 
              :value name 
              :id id 
              :disabled (or @group-is-empty @query-is-nonempty)
              :defaultChecked (not @group-is-empty)
              :on-click handle-filter-click}]
     [:label {:for id} name]]))

(defn filter-menu []
  [:div#filter-menu
   [:form
    (for [n language-group-names]
      ^{:key n} [filter-row n])]])

;; -- Search Bar --------------------------------------------------------------
(defn- handle-text-input
  [a _ _]
  (let [target (.-target a)
        query (.-value target)]
    (rf/dispatch [:update-query query])))

(defn search-bar []
  [:input#search-bar {:type "text"
                      :placeholder "Search"
                      :on-change handle-text-input}])

;; -- Put the right letters together and make a better day -------------------
(defn main-panel []
  (fn []
    (let [loaded (rf/subscribe [:loaded])]
      (if (not @loaded)
        [:div]
        [:div
         [:div.row.row-eq-height
          (let [current-point (rf/subscribe [:current-point])]
            [sidebar {:current-point @current-point}])
          [map-container]]
         [filter-menu]
         [search-bar]]))))

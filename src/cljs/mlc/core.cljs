(ns mlc.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [mlc.events]
            [mlc.subs]
            [mlc.views :as views]
            [mlc.config :as config]
            [day8.re-frame.http-fx]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root [] 
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))

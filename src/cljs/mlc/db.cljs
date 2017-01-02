(ns mlc.db
  (:require [mlc.mock-data :refer [mock-records]]))

(defonce language-group-names #{"European"
                                "African"
                                "Native American"
                                "South Asian"
                                "East Asian"
                                "Middle Eastern"
                                "Pacific"
                                "Other"})

(def default-db
  {:points mock-records
   :active-groups language-group-names})

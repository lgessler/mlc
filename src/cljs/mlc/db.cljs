(ns mlc.db
  (:require [cljs.spec :as s]))

(defn any [] true)

;; -- Spec --------------------------------------------------------------------
;; for points
(s/def ::lat number?)
(s/def ::lng number?)
(s/def ::name string?)
(s/def ::languages (s/coll-of string?))
(s/def ::language-groups (s/coll-of string?))
(s/def ::number-of-speakers string?)
(s/def ::type string?)
(s/def ::address string?)

(s/def ::point (s/keys :req-un [::lat 
                                ::lng 
                                ::name 
                                ::languages 
                                ::language-groups
                                ::number-of-speakers 
                                ::type 
                                ::address]
                       :opt-un [::marker]))

(s/def ::current-point #(or ::point (nil? %)))
(s/def ::points (s/* ::point))
(s/def ::markers (s/* any))
(s/def ::active-groups (s/* string?))
(s/def ::query string?)
(s/def ::loaded boolean?)

(s/def ::db (s/keys :req-un [::current-point ::points ::markers ::active-groups ::query ::loaded]))

;; -- Default -----------------------------------------------------------------
(defonce language-group-names #{"European"
                                "African"
                                "Native American"
                                "South Asian"
                                "East Asian"
                                "Middle Eastern"
                                "Pacific"
                                "Other"})

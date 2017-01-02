(ns mlc.mock-data)

(defn make-mock-record
  [lat lng name languages language-groups number-of-speakers type address]
  {:lat lat
   :lng lng
   :name name
   :languages languages
   :language-groups language-groups
   :number-of-speakers number-of-speakers
   :type type
   :address address})

(defonce mock-records
  [(make-mock-record 43.0719143
                     -89.391318
                     "Maharani Indian Restaurant"
                     #{"Hindi" "Urdu"}
                     #{"South Asian"}
                     "Most"
                     "Restaurant"
                     "380 W Washington Ave, Madison, WI 53703")
   (make-mock-record  43.0725961
                     -89.3910498
                     "Himal Chuli"
                     #{"Nepali"  "Hindi"}
                     #{"South Asian"}
                     "Some"
                     "Restaurant"
                     "318 State St  Madison  WI 53703")
   (make-mock-record  43.0731479
                     -89.3939771
                     "Casa de Lara"
                     #{"Spanish"}
                     #{"European"}
                     "One"
                     "Restaurant"
                     "341 State St #2  Madison  WI 53703")
   (make-mock-record  43.0651541
                     -89.5053459
                     "KJ's Curry Bowl"
                     #{"Sinhala"}
                     #{"South Asian"}
                     "Most"
                     "Restaurant"
                     "7005 Tree Ln  Madison  WI 53717")
   (make-mock-record 43.0874037
                     -89.3608845
                     "Banzo Shük"
                     #{"Turkish"}
                     #{"Middle Eastern"}
                     "Unknown"
                     "Restaurant"
                     "1511 Williamson St  Madison  WI 53703")
   (make-mock-record 43.0754945
                     -89.3926116
                     "Vientiane Palace"
                     #{"Lao"}
                     #{"East Asian"}
                     "All"
                     "Restaurant"
                     "151 W Gorham St  Madison  WI 53703")
   (make-mock-record  43.074724
                     -89.394792
                     "Palmyra Mediterranean Grill"
                     #{"Arabic"}
                     #{"Middle Eastern"}
                     "All"
                     "Restaurant"
                     "419 State St  Madison  WI 53703")
   (make-mock-record 43.0395449
                     -89.274315
                     "Ho-Chunk Gaming Madison"
                     #{"Ho-Chunk"}
                     #{"Native American"}
                     "Unknown"
                     "Casino"
                     "4002 Evan Acres Rd  Madison  WI 53718")])



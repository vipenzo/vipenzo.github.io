(ns io.embarassed.index
  (:require [io.embarassed.common :as common]
            [hiccup.page :refer [html5]]
            [hickory.core :refer (parse)]
            [hickory.convert :refer (hickory-to-hiccup)]
            [hiccup.core :refer (html)]))



(defn render [{global-meta :meta posts :entries}]
  (let [s (slurp "site-template/index.html")
        dom (parse s)
        ]
    (html5 dom)))





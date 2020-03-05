(ns io.embarassed.index
  (:require [io.embarassed.common :as common]
            [hiccup.page :refer [html5]]
            [hickory.core :refer (parse) :as hk]
            [hickory.convert :refer (hickory-to-hiccup)]
            [hickory.select :as s]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [hiccup.core :refer (html)]
            [clojure.string :as str]
            [java-time :as jt]
            ))

(comment def html-classes-map {
                               :headline       "post-summary"
                               :description    "post-description"
                               :title          "post-title"
                               :date-published "post-date"
                               :keywords       "post-categories"
                               :permalink      "post-link"
                               })

(defn published [{:keys [date-published date-created]}]
  (or date-published date-created))

(defn updated [{:keys [date-modified] :as post}]
  (or date-modified (published post)))

(defn iso-datetime [date]
  (java.util.Date/from (.toInstant date)))

(defn format-date [date]
  (let [df (java.text.SimpleDateFormat. "EEEE dd MMMMMMMMMM yyyy" java.util.Locale/ITALY)]
    (.format df date)))


(defn make-children-class-table [loc class-name-pattern]
  (loop [m {} loc (zip/down loc)]
    (if (nil? loc)
      m
      (let [node (zip/node loc)
            classes (some-> node
                            :attrs
                            :class
                            (str/split #" ")
                            ((fn [l] (filter #(re-find class-name-pattern %) l))))
            new-m (reduce #(assoc %1 %2 node) m classes)]
        (recur new-m (zip/right loc))))))

(defn update-entry
  ([parent-node selector content] (update-entry parent-node selector (some-> content vector) [:content]))
  ([parent-node selector content where]
   (-> (some-> (s/select-locs selector parent-node)
               first
               ((fn [loc]
                  (if (nil? content)
                    (zip/remove loc)
                    (zip/edit loc #(assoc-in % where content))
                    )))
               zip/root)
       (or parent-node))))


(defn make-child-node [template data-functions data]
  (when template
    (loop [node template [f & others] data-functions]
      (if (nil? f)
        node
        (recur (f node data) others)))))

(defn get-posts-class [post]
  (if ((set (:tags post)) "Blog")
    "post-template-noimage"
    "post-template-image"))


(defn list-render [node selector class-name-pattern entry-fn data-functions data]
  (let [loc (first (s/select-locs selector node))
        templates (make-children-class-table loc class-name-pattern)
        post-fn (fn [post]
                  (let [post-class (get-posts-class post)
                        template (templates post-class)]
                    (make-child-node template data-functions post)))
        entries (->> data
                     entry-fn
                     (map post-fn)
                     (filter some?))]
    (zip/root (zip/edit loc #(assoc-in % [:content] entries)))))

(defn page-render [node data]
  (let [data-functions [#(update-entry %1 (s/class "post-summary") (:headline %2))
                        #(update-entry %1 (s/class "post-title") (:title %2))
                        #(update-entry %1 (s/class "post-image") (:image %2) [:attrs :src])
                        #(update-entry %1 (s/class "post-date") (format-date (iso-datetime (updated %2))))
                        #(update-entry %1 (s/class "post-description") (:description %2))]]
    (-> node
        (list-render (s/class "landing-page") #"post-template" :entries data-functions data))))


(defn get-dom [file-path]
  (-> file-path
      slurp
      parse
      hk/as-hickory))


(defn render [{global-meta :meta posts :entries :as data}]
  (println "index render")
  (let [new-page (page-render (get-dom "site-template/index.html") data)]
    (hickory.render/hickory-to-html new-page)))


(comment
  (def ipage (hk/as-hickory (hk/parse (slurp "site-template/index.html"))))
  (def landing-page-loc (first (s/select-locs (s/class "landing-page") ipage)))
  (def templates (make-children-class-table landing-page-loc #"post-template"))
  (def data-functions [#(update-entry %1 (s/class "post-summary") (:headline %2))
                       #(update-entry %1 (s/class "post-title") (:title %2))
                       #(update-entry %1 (s/class "post-image") (:image %2))
                       #(update-entry %1 (s/class "post-description") (:description %2))])
  (def post-fn (fn [post]
                 (let [post-class (get-posts-class post)
                       template (templates post-class)]
                   (make-child-node template data-functions post))))
  (def posts [{:description    "Primo post",
               :path           "public/primo.html",
               :tags           '("prove"),
               :headline       nil,
               :author-email   "vipenzo@dovesonoio.com",
               :slug           "prova",
               :date-created   #inst "2020-02-20T00:00:00.000-00:00",
               :date-published #inst "2020-02-20T00:00:00.000-00:00",
               :content        "<h1><a href=\"#hello-world\" id=\"hello-world\"></a>Hello World</h1>\n<p>We are making a wundebar website!</p>\n",
               :permalink      "/primo.html",
               :date-modified  #inst "2020-02-20T00:00:00.000-00:00",
               :include-atom   true,
               :out-dir        "public",
               :full-path      "/home/ep/.boot/cache/tmp/home/ep/Documents/MyGithubBlog/vipenzo.github.io/site-project/2ix/-7bl902/public/prova.html",
               :parent-path    "public/",
               :keywords       "blog, test",
               :title          "Uncoso 1",
               :author         "Leonardo Nicotra",
               :image          "/images/cropped-homer2.jpg"
               :canonical-url  "http://localhost:3000/prova.html",
               :short-filename "prova",
               :extension      "html",
               :filename       "prova.html",
               :original-path  "prova.md",
               :include-rss    true,
               :uuid           "d24355f7-de07-4ae8-a79e-090f346eddf2",
               :in-language    "it",
               :location       "New York, USA&JET"}
              {:description    "Secondo post",
               :path           "public/secondo.html",
               :tags           '("Blog" "prove"),
               :headline       nil,
               :author-email   "asdrubl@dovesonoio.com",
               :slug           "prova",
               :date-created   #inst "2020-02-20T00:00:00.000-00:00",
               :date-published #inst "2020-02-20T00:00:00.000-00:00",
               :content        "<h1><a href=\"#hello-world\" id=\"hello-world\"></a>Hello World</h1>\n<p>We are making a wundebar website!</p>\n",
               :permalink      "/secondo.html",
               :date-modified  #inst "2020-02-20T00:00:00.000-00:00",
               :include-atom   true,
               :out-dir        "public",
               :full-path      "/home/ep/.boot/cache/tmp/home/ep/Documents/MyGithubBlog/vipenzo.github.io/site-project/2ix/-7bl902/public/prova.html",
               :parent-path    "public/",
               :keywords       "blog, test",
               :title          "Uncoso 2",
               :author         "Asdrubale Locascio",
               :canonical-url  "http://localhost:3000/prova.html",
               :short-filename "prova",
               :extension      "html",
               :filename       "prova.html",
               :original-path  "prova.md",
               :include-rss    true,
               :uuid           "d24355f7-de07-4ae8-a79e-090f346eddf2",
               :in-language    "it",
               :location       "New York, USA&JET"}])
  (def data {:posts posts})
  (def entries (->> data
                    :entries
                    (map post-fn)
                    (filter some?)))
  (def post (first posts))
  (published post)
  (jt/zoned-date-time (.toInstant (published post)))
  (jt/format (jt/format "dd-MM-yyyy HH:mm") (.toInstant (published post)))
  (.atZone (.toInstant (published post)) (jt/zone-id))
  (jt/format (jt/format "dd-MM-yyyy HH:mm") (.atZone (.toInstant (published post)) (jt/zone-id)))
  (jt/as (.toInstant (published post)) :year :month-of-year :day-of-month)
  (iso-datetime (published post))
  (doseq [x (range 7)]
    (println (format-datetime (jt/plus (iso-datetime (published post)) (jt/days x)))))
  (let [df (java.text.SimpleDateFormat. "EEEE dd MMMMMMMMMM yyyy" java.util.Locale/ITALY)]
    (.format df (iso-datetime (published post)))
    )
  (let [df (java.text.DateFormat/getDateInstance java.text.DateFormat/LONG java.util.Locale/ITALY)]
    (.format df (iso-datetime (published post))))
  (format-date (iso-datetime (published post)))

  (post-fn post)
  (get-posts-class post)
  (templates (get-posts-class post))
  (keys templates)
  (make-child-node (templates (get-posts-class post)) data-functions post)
  ((first data-functions) (templates (get-posts-class post)) post)
  (#(update-entry %1 (s/class "post-title") (:title %2)) (templates (get-posts-class post)) post)
  (update-entry (templates (get-posts-class post)) (s/class "post-description") (:headline post))
  (count entries)
  (let [new-page (page-render (get-dom "site-template/index.html") data)]
    (hickory.render/hickory-to-html new-page))

  (render data)
  )



(comment
  (def titolo (first (map hk/as-hickory (hk/parse-fragment "<h4 class=\"card-title post-title\">E Proprio Nel Vaticano</h4>\n"))))
  (def titolo (first (:content (hk/as-hickory (hk/parse-fragment "<h4 class=\"card-title post-title\">E Proprio Nel Vaticano</h4>\n")))))
  titolo
  (update-content (first (s/select-locs s/element titolo)) "filippo")


  (def posts [{:description    "Primo post",
               :path           "public/primo.html",
               :tags           '("Blog" "prove"),
               :headline       nil,
               :author-email   "vipenzo@dovesonoio.com",
               :slug           "prova",
               :date-created   #inst "2020-02-20T00:00:00.000-00:00",
               :date-published #inst "2020-02-20T00:00:00.000-00:00",
               :content        "<h1><a href=\"#hello-world\" id=\"hello-world\"></a>Hello World</h1>\n<p>We are making a wundebar website!</p>\n",
               :permalink      "/primo.html",
               :date-modified  #inst "2020-02-20T00:00:00.000-00:00",
               :include-atom   true,
               :out-dir        "public",
               :full-path      "/home/ep/.boot/cache/tmp/home/ep/Documents/MyGithubBlog/vipenzo.github.io/site-project/2ix/-7bl902/public/prova.html",
               :parent-path    "public/",
               :keywords       "blog, test",
               :title          "Uncoso 1",
               :author         "Leonardo Nicotra",
               :canonical-url  "http://localhost:3000/prova.html",
               :short-filename "prova",
               :extension      "html",
               :filename       "prova.html",
               :original-path  "prova.md",
               :include-rss    true,
               :uuid           "d24355f7-de07-4ae8-a79e-090f346eddf2",
               :in-language    "it",
               :location       "New York, USA&JET"}
              {:description    "Secondo post",
               :path           "public/secondo.html",
               :tags           '("prove"),
               :headline       nil,
               :author-email   "asdrubl@dovesonoio.com",
               :slug           "prova",
               :date-created   #inst "2020-02-20T00:00:00.000-00:00",
               :date-published #inst "2020-02-20T00:00:00.000-00:00",
               :content        "<h1><a href=\"#hello-world\" id=\"hello-world\"></a>Hello World</h1>\n<p>We are making a wundebar website!</p>\n",
               :permalink      "/secondo.html",
               :date-modified  #inst "2020-02-20T00:00:00.000-00:00",
               :include-atom   true,
               :out-dir        "public",
               :full-path      "/home/ep/.boot/cache/tmp/home/ep/Documents/MyGithubBlog/vipenzo.github.io/site-project/2ix/-7bl902/public/prova.html",
               :parent-path    "public/",
               :keywords       "blog, test",
               :title          "Uncoso 2",
               :author         "Asdrubale Locascio",
               :canonical-url  "http://localhost:3000/prova.html",
               :short-filename "prova",
               :extension      "html",
               :filename       "prova.html",
               :original-path  "prova.md",
               :include-rss    true,
               :uuid           "d24355f7-de07-4ae8-a79e-090f346eddf2",
               :in-language    "it",
               :location       "New York, USA&JET"}]
    )


  (def posts-nodes (map #(make-node (templates-map (get-posts-class %)) %) posts))
  posts-nodes


  (update-content-sel ipage (s/class "landing-page") posts-nodes)

  )





(comment
  (def ipage (hk/as-hickory (hk/parse (slurp "site-template/prova.html"))))
  (def landing-page-loc (first (s/select-locs (s/class "landing-page") ipage)))
  (make-children-class-table landing-page-loc #"post-template")


  (-> landing-page-loc
      zip/node
      :attrs)
  (-> landing-page-loc
      zip/down
      zip/right
      zip/node
      )


  (def templates-map (make-templates-map ipage))
  (map first (vals templates-map))
  (def no-templates-loc (remove-templates ipage))
  no-templates-loc
  (def template-loc (first (s/select-locs (s/descendant (s/class "post-template-dark")) ipage)))
  (zip/node template-loc)
  (make-node template-loc {"pasquale" "99" "post-summary" "33" "post-title" "22"})
  (def posts [["dark" {"post-summary" "11" "post-title" "111"}]
              ["dark" {"post-summary" "22" "post-title" "222"}]
              ["dark" {"pasquale" "99" "post-summary" "33" "post-title" "333"}]])
  (def new-page (add-posts no-templates-loc templates-map "landing-page" posts))
  (def add-fn (fn [acc [post-class post-data]]
                (-> acc
                    (#(s/select-locs (s/class "landing-page") %))
                    first
                    (zip/append-child (make-node (templates-map post-class) post-data))
                    zip/root
                    )))
  (add-fn no-templates-loc ["dark" {"post-summary" "22" "post-title" "222"}])
  (make-node template-loc {"post-summary" "22" "post-title" "222"})
  (make-node (templates-map "dark") {"post-summary" "22" "post-title" "222"})
  (templates-map "dark")
  (keys templates-map)
  (render {})

  last-page

  (def post {:description    "Un coso così.",
             :path           "public/prova.html",
             :tags           '("Blog" "prove"),
             :headline       nil,
             :author-email   "vipenzo@dovesonoio.com",
             :slug           "prova",
             :date-created   #inst "2020-02-20T00:00:00.000-00:00",
             :date-published #inst "2020-02-20T00:00:00.000-00:00",
             :content        "<h1><a href=\"#hello-world\" id=\"hello-world\"></a>Hello World</h1>\n<p>We are making a wundebar website!</p>\n",
             :permalink      "/prova.html",
             :date-modified  #inst "2020-02-20T00:00:00.000-00:00",
             :include-atom   true,
             :out-dir        "public",
             :full-path      "/home/ep/.boot/cache/tmp/home/ep/Documents/MyGithubBlog/vipenzo.github.io/site-project/2ix/-7bl902/public/prova.html",
             :parent-path    "public/",
             :keywords       "blog, test",
             :title          "Uncoso",
             :author         "Vincenzo Piombo",
             :canonical-url  "http://localhost:3000/prova.html",
             :short-filename "prova",
             :extension      "html",
             :filename       "prova.html",
             :original-path  "prova.md",
             :include-rss    true,
             :uuid           "d24355f7-de07-4ae8-a79e-090f346eddf2",
             :in-language    "it",
             :location       "New York, USA&JET"})

  (make-node (templates-map (get-posts-class post)) post)

  (-> no-templates-loc
      (#(s/select-locs (s/class "landing-page") %))
      first
      (zip/append-child (make-node template-loc {"pasquale" "99" "post-summary" "33" "post-title" "22"}))
      zip/root
      )


  (def prova-loc (first (s/select-locs (s/class "col-sm-3") ipage)))
  (def prova-loc (first (s/select-locs (s/class "col-sm-3") no-templates-loc)))
  (zip/node prova-loc)


  (def ipage-loc (first (s/select-locs (s/descendant (s/class "post-template-dark") (s/class "post-title")) ipage)))

  no-templates-loc
  (def mod-page (zip/root (zip/edit ipage-loc #(assoc-in % [:content] ["Primo Postone"]))))
  (s/select (s/descendant (s/class "post-template-dark") (s/class "post-title")) mod-page)
  (hk/as-hiccup mod-page)


  (def html_orig (hickory.render/hickory-to-html (zip/root ipage-loc)))
  (def html_mod (hickory.render/hickory-to-html mod-page))

  )


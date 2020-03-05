(set-env!
  :asset-paths #{"site-template/assets"}
  :source-paths #{"src" "resources"}
  :resource-paths #{"content"}
  :dependencies '[
                  [hiccup "1.0.5" :exclusions [org.clojure/clojure]]
                  [hickory "0.7.1" :exclusions [org.clojure/clojure]]
                  [clj-time "0.15.2"]
                  [clojure.java-time "0.3.2"]
                  [pandeiro/boot-http "0.8.3" :exclusions [org.clojure/clojure]]])



(require '[clojure.string :as str]
         '[boot.core :as boot]
         '[io.perun.meta :as pm]
         '[io.perun :as perun]
         '[io.perun.core :as cperun]
         '[hickory.core :as hk]
         '[java-time :as jt]
         '[io.embarassed.index :as index-view]
         '[io.embarassed.post :as post-view]
         '[pandeiro.boot-http :refer [serve]])



(deftask global-metadata
  "Read global metadata from `perun.base.edn` or configured file.

   The global metadata will be attached to fileset where it can be
   read and manipulated by the tasks. Render tasks will pass this
   as the first argument to render functions."
  [n filename NAME str "filename to read global metadata from"]
  (boot/with-pre-wrap fileset
    (let [meta-file (or filename "perun.base.edn")
          global-meta (some->> fileset
                               boot/ls
                               (boot/by-name [meta-file])
                               first
                               boot/tmp-file
                               slurp
                               read-string)]
      (cperun/report-info "global-metadata" "read global metadata from %s" global-meta)
      (let [fs (pm/set-global-meta fileset global-meta)]
        (cperun/report-info "DD" global-meta)
        ;(println (pm/get-global-meta fs))
        fs
        )
      )))

(deftask show-meta
         []
    (boot/with-pre-wrap fileset
         (println (pm/get-global-meta fileset))
         fileset))

(deftask percapire
         "Build test blog. This task is just for testing different plugins together."
         []
         (comp
           (perun/global-metadata)
           (perun/print-meta)
           (perun/markdown)
           (perun/render :renderer 'io.embarassed.post/render)
           (perun/collection :renderer 'io.embarassed.index/render :page "index.html")
           (show "-f")
           (target)

           )
         )

(deftask move-assets []
         (comp
           (sift "-m" "css/(.*)$:public/assets/css/$1")
           (sift "-m" "fonts/(.*)$:public/assets/fonts/$1")
           (sift "-m" "img/(.*)$:public/assets/img/$1")
           (sift "-m" "js/(.*)$:public/assets/js/$1")
           (sift "-m" "bootstrap/public/assets/(.*)$:public/assets/bootstrap/$1")
           ))

(deftask build-from-template
         "build the site modifying site-template"
         []
         (comp
           (perun/global-metadata)
           (perun/print-meta)
           (perun/markdown)
           ;(perun/render :renderer 'io.embarassed.post/render)
           (perun/collection :renderer 'io.embarassed.index/render :page "index.html")
           (move-assets)
           (target "-d" "../target")
           ))

(deftask mydev
         []
         (comp (watch)
               (build-from-template)
               (serve "-d" "../target")))

(deftask build
  "Build test blog. This task is just for testing different plugins together."
  []
  (comp
        (perun/global-metadata)
        (perun/markdown)
        (perun/draft)
        (perun/print-meta)
        (perun/slug)
        (perun/ttr)
        (perun/word-count)
        (perun/build-date)
        (perun/gravatar :source-key :author-email :target-key :author-gravatar)
        (perun/render :renderer 'io.embarassed.post/render)
        (perun/collection :renderer 'io.embarassed.index/render :page "index.html")
        (perun/tags :renderer 'io.embarassed.tags/render)
        (perun/paginate :renderer 'io.embarassed.paginate/render)
        (perun/assortment :renderer 'io.embarassed.assortment/render
                          :grouper (fn [entries]
                                     (->> entries
                                          (mapcat (fn [entry]
                                                    (if-let [kws (:keywords entry)]
                                                      (map #(-> [% entry]) (str/split kws #"\s*,\s*"))
                                                      [])))
                                          (reduce (fn [result [kw entry]]
                                                    (let [path (str kw ".html")]
                                                      (-> result
                                                          (update-in [path :entries] conj entry)
                                                          (assoc-in [path :entry :keyword] kw))))
                                                  {}))))
        (perun/static :renderer 'io.embarassed.about/render :page "about.html")
        (perun/inject-scripts :scripts #{"start.js"})
        (perun/sitemap)
        (perun/rss :description "Hashobject blog")
        (perun/atom-feed :filterer :original)
        (perun/print-meta)
        (target)
        (notify)))

(deftask dev
  []
  (comp (watch)
        (build)
        (serve :resource-root "public")))

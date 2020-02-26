(ns io.embarassed.common
  (:use [hiccup.core :only (html)]
        [hiccup.page :only (html5)])
  )


(def header
  [:header#masthead.site-header {:role "banner"}
   [:div.custom-header
    [:div.custom-header-media
     [:div#wp-custom-header.wp-custom-header [:img {:src "http://www.limbarazzodellasceta.it/wp/wp-content/uploads/2019/04/cropped-homer2.jpg" :alt "L'Imbarazzo dell'Asceta" :srcset "http://www.limbarazzodellasceta.it/wp/wp-content/uploads/2019/04/cropped-homer2.jpg 2000w, http://www.limbarazzodellasceta.it/wp/wp-content/uploads/2019/04/cropped-homer2-300x231.jpg 300w, http://www.limbarazzodellasceta.it/wp/wp-content/uploads/2019/04/cropped-homer2-768x592.jpg 768w, http://www.limbarazzodellasceta.it/wp/wp-content/uploads/2019/04/cropped-homer2-1024x789.jpg 1024w" :sizes "100vw" :width "2000" :height "1541"}]]]
    [:div.site-branding {:style "margin-bottom: 72px;"}
     [:div.wrap
      [:div.site-branding-text
       [:h1.site-title [:a {:href "http://www.limbarazzodellasceta.it/wp/" :rel "home"} "L'Imbarazzo dell'Asceta"]]
       [:p.site-description "quattro chiacchiere su spiritualità, tecnologia e altro"]]]]]
   [:div.navigation-top
    [:div.wrap
     [:nav#site-navigation.main-navigation {:role "navigation" :aria-label "Menu in alto"}
      [:button.menu-toggle {:aria-controls "top-menu" :aria-expanded "false"}
       [:svg.icon.icon-bars {:aria-hidden "true" :role "img"} [:use {:href "#icon-bars" :xlink:href "#icon-bars"}]] [:svg.icon.icon-close {:aria-hidden "true" :role "img"} [:use {:href "#icon-close" :xlink:href "#icon-close"}]] "Menu"]
      [:div.menu-menu-in-alto-container [:ul#top-menu.menu [:li#menu-item-486.menu-item.menu-item-type-post_type.menu-item-object-page.menu-item-486 [:a {:href "http://www.limbarazzodellasceta.it/wp/aaa/"} "Content"]]
                                         [:li#menu-item-487.menu-item.menu-item-type-taxonomy.menu-item-object-category.menu-item-487 [:a {:title "Spiritualità" :href "http://www.limbarazzodellasceta.it/wp/category/spiritualita/"} "spiritualità"]]
                                         [:li#menu-item-488.menu-item.menu-item-type-taxonomy.menu-item-object-category.menu-item-488 [:a {:title "Evoluzione Contro Cultura" :href "http://www.limbarazzodellasceta.it/wp/category/evoluzione-cultura/"} "evoluzione-cultura"]]
                                         [:li#menu-item-491.menu-item.menu-item-type-taxonomy.menu-item-object-category.menu-item-491 [:a {:href "http://www.limbarazzodellasceta.it/wp/category/cosetecnicose/"} "cosetecnicose"]]
                                         [:li#menu-item-125.menu-item.menu-item-type-taxonomy.menu-item-object-category.menu-item-125 [:a {:href "http://www.limbarazzodellasceta.it/wp/category/blog/"} "Blog"]]]]
      [:a.menu-scroll-down {:href "#content"} [:svg.icon.icon-arrow-right {:aria-hidden "true" :role "img"} [:use {:href "#icon-arrow-right" :xlink:href "#icon-arrow-right"}]] [:span.screen-reader-text "Vai al contenuto"]]]]]])

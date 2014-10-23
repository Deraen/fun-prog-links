(ns fun-prog-links.core
  (:require [clj-time.core :as t]
            [clj-time.format :as cf]
            [clj-time.local :as cl]
            [clojure.string :as string]
            [compojure.api.sweet :refer :all]
            [garden.core :refer [css]]
            [garden.units :as gu :refer [px]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css]]
            [monger.collection :as mc]
            [monger.core :as mg]
            monger.joda-time
            [monger.query :as mq]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [swiss.arrows :refer :all])
  (:import [org.joda.time DateTime]
           [org.bson.types ObjectId]))

;; DB
(let [m (mg/connect-via-uri (or (System/getenv "MONGOLAB_URI") "mongodb://127.0.0.1/fun-prog-links"))]
  (def conn (:conn m))
  (def db (:db m)))

(defn create-id []
  (str (ObjectId.)))

;; API
(s/defschema Link {:_id s/Str
                   :uri s/Str
                   :nick s/Str
                   :tags #{s/Keyword}
                   :timestamp DateTime})

(s/defschema NewLink (-> Link
                         (dissoc :_id :timestamp)
                         (assoc (s/optional-key :timestamp) DateTime)))

;; Domain fns
(defn find-links [& [q]]
  (mq/with-collection
    db "links"
    (mq/find (or q {}))
    (mq/sort {:timestamp -1})))

(defn insert-link [link]
  (-<> link
       ; Note: create-id manually to use string instead of UUID
       (assoc :_id (create-id))
       ; Default timestamp
       (update-in [:timestamp] #(or % (DateTime.)))
       (mc/insert-and-return db :links <>)))

(def time-format (cf/formatter "dd.MM.yyyy HH:mm" (t/default-time-zone)))
(defn date->str [datetime]
  (cf/unparse time-format datetime))

(defn prettify-url [url]
  (string/replace url #"^http[s]?:\/\/" ""))

;; "Frontend"

(defn index []
  (html
    (html5
      [:head
       [:title "TUT Fun prog – links"]
       [:meta {:charset "utf-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
       (include-css "/main.css")]
      [:body
       [:a {:href "https://github.com/Deraen/fun-prog-links"}
        [:img {:style "position: absolute; top: 0; right: 0; border: 0;"
               :src "https://camo.githubusercontent.com/365986a132ccd6a44c23a9169022c0b5c890c387/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f7265645f6161303030302e706e67"
               :alt "Fork me on GitHub"
               :data-canonical-src "https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png"}]]
       [:div.container
        [:header [:h1 "Fun Prog Links"]]
        [:main
         [:ul
          (for [{:keys [uri nick tags timestamp]}
                (find-links)]
            [:li
             [:a.link {:href uri
                       :target "new"} (prettify-url uri)]
             [:span.tags
              (for [tag (sort tags)]
                [:span.tag ":" tag])]
             [:span.nick nick]
             [:span.time "@" (date->str timestamp)]])]]
        [:footer [:a {:href "/docs"} "API docs"]]]])))

(def bg-color "#1c1c1c")
(def link-color "#ccc")

(def color1 "#4d98a5")
(def color2 "#5073dd")

(def style
  (css
    ["@font-face" {
                   :font-family "\"Anonymous Pro\""
                   :font-style :normal;
                   :font-weight 400;
                   :src "local('Anonymous Pro'), local('AnonymousPro'), url(http://fonts.gstatic.com/s/anonymouspro/v8/Zhfjj_gat3waL4JSju74E0bTF2-gLvP1ecKBiMhtO8o.woff2) format('woff2');"
                   :unicode-range "U+0000-00FF, U+0131, U+0152-0153, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2212, U+2215, U+E0FF, U+EFFD, U+F000;"}]

    [:body {:background bg-color
            :color "#fff"
            :font-family "\"Anonymous Pro\""}]

    [:footer {:margin-top (px 40)}]

    [:.container {:width (px 800)
                  :margin "0 auto"}]

    [:a {:color link-color}]

    [:ul {:margin 0
          :padding 0}
     [:li {:list-style :none
           :line-height (px 32)}]]

    [:.link {:font-size (px 18)
             :text-decoration :none
             :color color2}]
    [:span.tags {:margin "0 10px"}]
    [:span.tag {:background color1
                :border-radius (px 4)
                :padding "2px 4px"
                :margin-right "5px"}]
    [:span.nick {:margin-right (px 10)}]
    [:span.time {:color "#888"}]))

;; Http handler
(defapi app
  (GET* "/" []
    (-> (ok (index))
        (content-type "text/html; chaarset=UTF-8")))
  (GET* "/main.css" []
    (-> (ok style)
        (content-type "text/css")))
  (swagger-ui "/docs")
  (swagger-docs
    :title "Fun-prog-links Api"
    :description "this is Fun-prog-links Api.")
  (swaggered "links"
    :description "Link endpoints"
    (GET* "/links" []
      :summary "List all links"
      :return [Link]
      (ok (find-links {})))
    (POST* "/links" []
      :summary "Create a new link"
      :body [link NewLink]
      (ok (insert-link link)))))

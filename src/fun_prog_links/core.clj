(ns fun-prog-links.core
  (:require [clj-time.core :as t]
            [clj-time.format :as cf]
            [clj-time.local :as cl]
            [clojure.string :as string]
            [compojure.api.sweet :refer :all]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css]]
            [monger.collection :as mc]
            [monger.core :as mg]
            monger.joda-time
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [swiss.arrows :refer :all])
  (:import [org.joda.time DateTime]
           [org.bson.types ObjectId]))

;; DB
(let [m (mg/connect-via-uri (or (System/getenv "MONOHQ_URI") "mongodb://127.0.0.1/fun-prog-links"))]
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
(defn coerce-link [link]
  link)

(defn find-links [& [q]]
  (->> (mc/find-maps db :links (or q {}))
       (map coerce-link)))


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
       (include-css "style.css")]
      [:body
       [:h1 "Links"]
       [:ul
        (for [{:keys [uri nick tags timestamp]}
              (find-links)]
          [:li
           [:a.link {:href uri} (prettify-url uri)]
           [:span.tags
            (for [tag tags]
              [:span.tag ":" tag])]
           [:span.nick nick]
           [:span.time "@" (date->str timestamp)]])]
       [:a {:href "/docs"} "API docs"]])))

;; Http handler
(defapi app
  (GET* "/" []
    (ok (index)))
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

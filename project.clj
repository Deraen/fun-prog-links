(defproject fun-prog-links "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://github.com/metosin/fun-prog-links"
  :scm {:name "git"
        :url "http://github.com/metosin/fun-prog-links"}
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [http-kit "2.1.16"]
                 [metosin/compojure-api "0.16.2"]
                 [metosin/ring-http-response "0.5.1"]
                 [metosin/ring-swagger-ui "2.0.17"]
                 [hiccup "1.0.5"]
                 [com.novemberain/monger "2.0.0"]
                 [clj-time "0.8.0"]
                 [swiss-arrows "1.0.0"]
                 [garden "1.2.3"]]
  :ring {:handler fun-prog-links.core/app}
  :uberjar-name "server.jar"
  :profiles {:uberjar {:resource-paths ["swagger-ui"]}
             :dev {:plugins [[lein-ring "0.8.13"]]
                   :dependencies [[javax.servlet/servlet-api "2.5"]]}})

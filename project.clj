(defproject fun-prog-links "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.6.0"]

                 [metosin/compojure-api "0.16.2"]
                 [metosin/ring-http-response "0.5.1"]
                 [metosin/ring-swagger-ui "2.0.17"]
                 [hiccup "1.0.5"]
                 [com.novemberain/monger "2.0.0"]
                 [clj-time "0.8.0"]
                 [swiss-arrows "1.0.0"]

                 ; Frontend
                 ; [circleci/stefon "0.5.0-SNAPSHOT"]
                 #_[org.webjars/bootstrap "3.2.0"]]
  :ring {:handler fun-prog-links.core/app}
  :uberjar-name "server.jar"
  :profiles {:uberjar {:resource-paths ["swagger-ui"]}
             :dev {:plugins [[lein-stefon-precompile "0.5.0"]
                             [lein-ring "0.8.13"]]
                   :dependencies [[javax.servlet/servlet-api "2.5"]]}})

(ns fun-prog-links.server
  (:require [fun-prog-links.core :refer :all]
            [org.httpkit.server :refer :all]))

(defn -main [& [port]]
  (run-server app {:port (or port (some-> (System/getenv "PORT") Integer.) 8080)}))

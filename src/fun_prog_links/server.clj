(ns fun-prog-links.server
  (:require [fun-prog-links.core :refer :all]
            [org.httpkit.server :refer :all]))

(defn -main [& [port]]
  (run-server app {:port (or port (System/getenv "PORT") 8080)}))

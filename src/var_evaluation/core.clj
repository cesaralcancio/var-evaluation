(ns var-evaluation.core
  (:require
    [reitit.ring :as ring]
    [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn file-handler
  [false-file]
  (println "file-handler")
  {:status 200
   :body   (str "File: " (:file false-file))})

(defn wrap-path-param
  [handler param]
  (println "wrap-path-param")
  (fn [request]
    (println "wrap-path-param nested fn")
    (handler {:file (get-in request [:path-params param])})))

(def not-found
  (identity (fn [r] {:body "File not found!" :status 404})))

(def router
  (ring/ring-handler
    (ring/router
      [["/not-found" {:get not-found :name ::not-found}]
       ["/articles/:file" {:get (wrap-path-param file-handler :file) :name ::articles}]
       ["/articles/v2/:file" {:get (wrap-path-param file-handler :file) :name :articles-v2}]])
    (ring/create-default-handler {:not-found not-found})))

(def server (atom nil))

(defn stop-server!
  []
  (swap! server #(do (.stop %) nil)))

(defn -main
  []
  (reset! server (run-jetty (var router) {:port 8080 :join? false})))

(stop-server!)
(-main)

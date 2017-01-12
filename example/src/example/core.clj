(ns example.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [cheshire.core :refer [parse-stream generate-stream generate-string parse-string]]
            [clojure.java.io :as io]
            [ring.util.codec :as codec]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params
                                          wrap-json-response]]
            [ring.util.response :as r]
            [compojure.core :refer :all]))

(defn interpolate-path [params path]
  (reduce (fn -interpolate-path [acc [key value]]
            (clojure.string/replace acc
                         (re-pattern (format "\\{%s\\}" (name key)))
                         value))
          path
          params))

(defroutes app
  (GET "/v1" {params :params}
       (let [name (get params :name "World")]
         (println "params" params)
         (-> (r/response {:message (format "Hello, %s" name)})))))

(def handler (wrap-json-response
               (wrap-json-params
                 (wrap-params
                   (wrap-keyword-params
                     app)))))

(defn- generate-query-string [params]
  (clojure.string/join "&" (map (fn [[k v]] (str (name k) "=" v)) params)))

(generate-query-string {:name "a"})

(deflambdafn example.LambdaFn [is os ctx]
  (with-open [writer (io/writer os)]
    (let [payload (parse-stream (io/reader is :encoding "UTF-8") true)
          request {:uri (:path payload)
                   :query-string (generate-query-string (:queryStringParameters payload))
                   :request-method :get}]
      (println payload)
      (let [output (handler request)]
        (println output)
        (generate-stream {:statusCode (:status output)
                          :headers (:headers output)
                          :body (:body output)}
                         writer)))))

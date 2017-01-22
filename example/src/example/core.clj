(ns example.core
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [cheshire.core :refer [parse-stream generate-stream generate-string parse-string]]
            [clojure.java.io :as io]
            [ring.util.codec :as codec]
            [ring.middleware.apigw :refer [wrap-apigw-lambda-proxy]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params
                                          wrap-json-response]]
            [ring.util.response :as r]
            [compojure.core :refer :all]))

(defroutes app
  (GET "/v1/hello" {params :params}
       (let [name (get params :name "World")]
         (-> (rresponse {:message (format "Hello, %s" name)})))))

(def handler (wrap-apigw-lambda-proxy
               (wrap-json-response
                 (wrap-json-params
                   (wrap-params
                     (wrap-keyword-params
                       app))))))

(deflambdafn example.LambdaFn [is os ctx]
  (with-open [writer (io/writer os)]
    (let [payload (parse-stream (io/reader is :encoding "UTF-8") true)]
      (let [output (handler payload)]
        (generate-stream output writer)))))

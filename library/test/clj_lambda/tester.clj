(ns clj-lambda.tester
  (:require [clj-lambda.aws :refer [install-lambda uninstall-lambda]]))

(defn- test-config [bucket-name]
  {:handler "lambda-demo.LambdaFn"
   :memory-size 512
   :timeout 60
   :function-name "my-func-test2"
   :region "eu-west-1"
   :s3 {:bucket bucket-name
        :object-key "lambda.jar"}})

;(let [bucket-name "my-testing-123456"]
;  (install-lambda "test" [(test-config bucket-name)] "resources/empty.jar")
;  (uninstall-lambda "test" [(test-config bucket-name)]))

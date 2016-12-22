(defproject clj-lambda "0.4.0"
  :description "Clojure utilities for AWS Lambda deployment"
  :url "https://github.com/mhjort/lein-clj-lambda/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.6.3"]
                 [com.fasterxml.jackson.core/jackson-core "2.8.4"]
                 [com.fasterxml.jackson.core/jackson-databind "2.8.4"]
                 [com.amazonaws/aws-java-sdk-core "1.11.52" :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                 [com.amazonaws/aws-java-sdk-lambda "1.11.52"]
                 [com.amazonaws/aws-java-sdk-iam "1.11.52"]
                 [com.amazonaws/aws-java-sdk-api-gateway "1.11.52"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.52"]])

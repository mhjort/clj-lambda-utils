(defproject clj-lambda "0.3.1"
  :description "Clojure utilities for AWS Lambda deployment"
  :url "https://github.com/mhjort/lein-clj-lambda/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.5.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.5.3"]
                 [com.fasterxml.jackson.core/jackson-databind "2.5.3"]
                 [com.amazonaws/aws-java-sdk-core "1.10.77" :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.77"]
                 [com.amazonaws/aws-java-sdk-iam "1.10.77"]
                 [com.amazonaws/aws-java-sdk-api-gateway "1.10.77"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.77"]])

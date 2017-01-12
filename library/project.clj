(def aws-java-sdk-version "1.11.76")
(defproject clj-lambda "0.5.1"
  :description "Clojure utilities for AWS Lambda deployment"
  :url "https://github.com/mhjort/lein-clj-lambda/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.6.3"]
                 [com.fasterxml.jackson.core/jackson-core "2.5.3"]
                 [com.fasterxml.jackson.core/jackson-databind "2.5.3"]
                 [prismatic/schema "1.1.3"]
                 [com.amazonaws/aws-java-sdk-core ~aws-java-sdk-version
                   :exclusions [com.fasterxml.jackson.core/jackson-databind]]
                 [com.amazonaws/aws-java-sdk-lambda ~aws-java-sdk-version]
                 [com.amazonaws/aws-java-sdk-iam ~aws-java-sdk-version]
                 [com.amazonaws/aws-java-sdk-api-gateway ~aws-java-sdk-version]
                 [com.amazonaws/aws-java-sdk-s3 ~aws-java-sdk-version]])

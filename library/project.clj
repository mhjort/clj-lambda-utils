(defproject clj-lambda "0.1.0"
  :description "Clojure utilities for AWS Lambda deployment"
  :url "https://github.com/mhjort/lein-clj-lambda/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.6.1"]
                 [com.amazonaws/aws-java-sdk-core "1.10.77"]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.77"]
                 [com.amazonaws/aws-java-sdk-iam "1.10.77"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.77" :exclusions [com.amazonaws/aws-java-sdk-core]]])

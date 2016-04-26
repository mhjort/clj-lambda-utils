(defproject lein-clj-lambda "0.3.0"
  :description "Leiningen plugin for AWS Lambda deployment"
  :url "https://github.com/mhjort/clj-lambda-deploy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.6.1"]
                 [com.amazonaws/aws-java-sdk-lambda "1.10.60"]
                 [com.amazonaws/aws-java-sdk-core "1.10.60"]
                 [com.amazonaws/aws-java-sdk-iam "1.10.60"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.60"]]
  :eval-in-leiningen true)

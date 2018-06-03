(defproject clj-lambda "0.7.0"
  :description "Clojure utilities for AWS Lambda deployment"
  :url "https://github.com/mhjort/lein-clj-lambda/library"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [prismatic/schema "1.1.4"]
                 [com.amazonaws/aws-java-sdk-bundle "1.11.112"]
                 [robert/bruce "0.8.0"]])

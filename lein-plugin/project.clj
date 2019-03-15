(defproject lein-clj-lambda "0.12.1"
  :description "Leiningen plugin for AWS Lambda deployment"
  :url "https://github.com/mhjort/clj-lambda-deploy"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-lambda "0.8.1"]
                 [org.clojure/tools.cli "0.3.5"]]
  :min-lein-version "2.7.1"
  :eval-in-leiningen true)

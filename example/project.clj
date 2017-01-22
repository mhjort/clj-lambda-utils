(defproject example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring/ring-core "1.5.1"]
                 [ring-apigw-lambda-proxy "0.0.2"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.4.0"]
                 [uswitch/lambada "0.1.0"]
                 [cheshire "5.6.1"]]
  :plugins [[lein-clj-lambda "0.9.1"]]
  :lambda  {"example" [{:api-gateway {:name "UtilsExampleApi"}
                        :handler "example.LambdaFn" ;Do not change this
                        :memory-size 1536 ;Do not change this
                        :timeout 30 ;Do not change this
                        :function-name "lambda-utils-example" ;Do not change this
                        :region "eu-west-1"}]}
  :aot :all)

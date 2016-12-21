(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-lambda.aws :as aws]))

(defn lambdatask [f project environment flags]
   (let [deployments (get-in project [:lambda environment])
         jar-file (uberjar project)]
     (when (empty? deployments)
       (throw (ex-info "Could not find anything to install or deploy" {:environment environment})))
     (f environment deployments jar-file flags)))

(defn update-lambda-task [project environment flags]
   (lambdatask aws/update-lambda project environment flags))

(defn install-lambda-task [project environment flags]
  (lambdatask aws/install-lambda project environment flags))

(def flags
  [["-o" "--only-api-gateway" "Apply only API Gateway changes"]])

(defn lambda [project task environment & args]
  (let [opts (:options (parse-opts args flags))]
    (condp = task
      "update" (update-lambda-task project environment opts)
      "install" (install-lambda-task project environment opts)
      (println "Currently only tasks 'update' and 'install' are supported."))))

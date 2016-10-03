(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [clj-lambda.aws :as aws])
  (:import [java.io File]))

(defn lambdatask [f project environment]
   (let [deployments (get-in project [:lambda environment])
         jar-file (uberjar project)]
     (when (empty? deployments)
       (throw (ex-info "Could not find anything to install or deploy" {:environment environment})))
     (f deployments jar-file)))

(defn update-lambda-task [project environment]
   (lambdatask aws/update-lambda project environment))

(defn install-lambda-task [project environment]
  (lambdatask aws/install-lambda project environment))

(defn lambda [project & [task environment]]
  (condp = task
    "update" (update-lambda-task project environment)
    "install" (install-lambda-task project environment)
    (println "Currently only tasks 'update' and 'install' are supported.")))

(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [clj-lambda.aws :as aws])
  (:import [java.io File]))

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

(defn lambda [project & [task environment flag]]
  (condp = task
    "update" (update-lambda-task project environment [flag])
    "install" (install-lambda-task project environment [flag])
    (println "Currently only tasks 'update' and 'install' are supported.")))

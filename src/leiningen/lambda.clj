(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [leiningen.aws :as aws])
  (:import [java.io File]))

(defn- update-lambda [project environment]
  (let [deployments (get-in project [:lambda environment])
        jar-file (uberjar project)]
    (when (empty? deployments)
      (throw (ex-info "Could not find anything to update" {:environment environment})))
    (doseq [{:keys [region function-name s3]} deployments]
      (let [{:keys [bucket object-key]} s3]
        (println "Deploying to region" region)
        (aws/store-jar-to-bucket (File. jar-file)
                                 bucket
                                 object-key)
        (aws/update-lambda-fn function-name bucket region object-key)))))

(defn- install-lambda [project environment]
  (let [deployments (get-in project [:lambda environment])
        jar-file (uberjar project)]
    (when (empty? deployments)
      (throw (ex-info "Could not find anything to install" {:environment environment})))
    (doseq [{:keys [region function-name handler memory-size timeout s3 policy-statements] :as deployment} deployments]
      (let [{:keys [bucket object-key]} s3
            role-arn (aws/create-role-and-policy (str function-name "-role")
                                                 (str function-name "-policy")
                                                 policy-statements)]

        (println "Installing to region" region)
        (aws/create-bucket-if-needed bucket region)
        (aws/store-jar-to-bucket (File. jar-file)
                                 bucket
                                 object-key)
        (aws/create-lambda-fn (assoc deployment :role-arn role-arn))))))

(defn lambda [project & [task environment]]
  (condp = task
    "update" (update-lambda project environment)
    "install" (install-lambda project environment)
    (println "Currently only tasks 'update' and 'install' are supported.")))

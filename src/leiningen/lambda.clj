(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]])
  (:import [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.lambda.model UpdateFunctionCodeRequest]
           [com.amazonaws.services.lambda AWSLambdaClient]
           [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.regions Regions]
           [java.io File]))

(def aws-credentials
  (.getCredentials (DefaultAWSCredentialsProviderChain.)))

(defonce s3-client
  (delay (AmazonS3Client. aws-credentials)))

(defn- create-lambda-client [region]
  (-> (AWSLambdaClient. aws-credentials)
      (.withRegion (Regions/fromName region))))

(defn- store-jar-to-bucket [^File jar-file bucket-name object-key]
  (println "Uploading code to S3 bucket" bucket-name "with name" object-key)
  (.putObject @s3-client
              bucket-name
              object-key
              jar-file))

(defn- update-lambda-fn [lambda-name bucket-name region object-key]
  (println "Updating Lambda function" lambda-name "in region" region)
  (let [client (create-lambda-client region)]
    (.updateFunctionCode client (-> (UpdateFunctionCodeRequest.)
                                    (.withFunctionName lambda-name)
                                    (.withS3Bucket bucket-name)
                                    (.withS3Key object-key)))))

(defn lambda [project & [task environment]]
  (if (= "update" task)
    (let [deployments (get-in project [:lambda environment])
          jar-file (uberjar project)]
      (doseq [{:keys [region function-name s3]} deployments]
        (let [{:keys [bucket object-key]} s3]
          (println "Deploying to region" region)
          (store-jar-to-bucket (File. jar-file)
                               bucket
                               object-key)
          (update-lambda-fn function-name bucket region object-key))))
    (println "Currently only task 'update' is supported.")))

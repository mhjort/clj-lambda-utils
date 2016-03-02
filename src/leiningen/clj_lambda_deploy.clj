(ns leiningen.clj-lambda-deploy
  (:require [leiningen.uberjar :refer [uberjar]])
  (:import [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.regions Regions]
           [java.io File]))

(def aws-credentials
  (.getCredentials (DefaultAWSCredentialsProviderChain.)))

(defonce s3-client
  (delay (AmazonS3Client. aws-credentials)))

(defn- store-jar-to-bucket [^File jar-file bucket-name object-key]
  (println "Uploading code to S3 bucket" bucket-name "with name" object-key)
  (.putObject @s3-client
              bucket-name
              object-key
              jar-file))

(defn clj-lambda-deploy [project & args]
  (println project)
  (let [jar-file (uberjar project)]
    (store-jar-to-bucket (File. jar-file)
                         (get-in project [:lambda :s3 :bucket])
                         (get-in project [:lambda :s3 :key]))))

(ns clj-lambda.aws
  (:require [clj-lambda.iam :as iam]
            [clj-lambda.api-gateway :as ag]
            [clj-lambda.schema :refer [OptionsSchema
                                       ConfigSchemaForUpdate
                                       ConfigSchemaForInstall]]
            [schema.core :as s])
  (:import [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.lambda.model CreateFunctionRequest
                                                UpdateFunctionCodeRequest
                                                FunctionCode
                                                Environment]
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

(defn create-bucket-if-needed [bucket-name region]
  (if (.doesBucketExist @s3-client bucket-name)
    (println bucket-name "S3 bucket already exists. Skipping creation.")
    (do (println "Creating bucket" bucket-name "in region" region ".")
        (if (= "us-east-1" region)
          (.createBucket @s3-client bucket-name)
          (.createBucket @s3-client bucket-name region)))))

(defn store-jar-to-bucket [^File jar-file bucket-name object-key]
  (println "Uploading code to S3 bucket" bucket-name "with name" object-key)
  (.putObject @s3-client
              bucket-name
              object-key
              jar-file))

(defn create-lambda-fn [{:keys [function-name handler bucket
                                object-key environment memory-size
                                timeout region role-arn]}]
  (println "Creating Lambda function" function-name "to region" region)
  (let [client (create-lambda-client region)
        env-vars (.withVariables (Environment.) environment)]
    (.createFunction client (-> (CreateFunctionRequest.)
                                (.withFunctionName function-name)
                                (.withMemorySize (int memory-size))
                                (.withTimeout (int timeout))
                                (.withRuntime "java8")
                                (.withHandler handler)
                                (.withCode (-> (FunctionCode.)
                                               (.withS3Bucket bucket)
                                               (.withS3Key object-key)))
                                (.withEnvironment env-vars)
                                (.withRole role-arn)))))

(defn update-lambda-fn [lambda-name bucket-name region object-key]
  (println "Updating Lambda function" lambda-name "in region" region)
  (let [client (create-lambda-client region)]
    (.updateFunctionCode client (-> (UpdateFunctionCodeRequest.)
                                    (.withFunctionName lambda-name)
                                    (.withS3Bucket bucket-name)
                                    (.withS3Key object-key)))))

(defn- deployment-s3-config [user-s3-config function-name]
  (if user-s3-config
    user-s3-config
    (let [default-s3-config {:bucket (str function-name "-" (iam/get-account-id))
                             :object-key (str function-name ".jar")}]
      (println "No S3 settings defined, using defaults" default-s3-config)
      default-s3-config)))

(defn- validate-input [config-schema config opts]
  (s/validate config-schema config)
  (s/validate OptionsSchema opts))

(defn update-lambda [stage-name config jar-file & [opts]]
  (validate-input ConfigSchemaForUpdate config (or opts {}))
  (println "Updating env" stage-name "with options" opts)
  (let [[{:keys [region function-name s3]}] config
        {:keys [bucket object-key]} (deployment-s3-config s3 function-name)]
    (println "Deploying to region" region)
    (store-jar-to-bucket (File. jar-file)
                         bucket
                         object-key)
    (update-lambda-fn function-name bucket region object-key)))

(defn install-lambda [stage-name config jar-file & [opts]]
  (validate-input ConfigSchemaForInstall config (or opts {}))
  (println "Installing env" stage-name "with options" opts)
  (let [[{:keys [api-gateway region function-name environment
                 handler memory-size timeout s3 policy-statements] :as env-settings}] config
        install-all? (not (:only-api-gateway opts))]
    (println "Installing with settings" env-settings)
    (when api-gateway
      (ag/setup-api-gateway stage-name (:name api-gateway) region function-name))
    (if install-all?
      (let [{:keys [bucket object-key]} (deployment-s3-config s3 function-name)
            role-arn (iam/create-role-and-policy (str function-name "-role")
                                                 (str function-name "-policy")
                                                 "lambda.amazonaws.com"
                                                 (iam/log-policy-with-statements policy-statements))]

        (create-bucket-if-needed bucket region)
        (store-jar-to-bucket (File. jar-file)
                             bucket
                             object-key)
        (create-lambda-fn (-> env-settings
                              (select-keys [:function-name :handler :timeout
                                            :environment :memory-size :region])
                              (assoc :role-arn role-arn :bucket bucket :object-key object-key))))
      (println "Skipping Lambda installation"))))

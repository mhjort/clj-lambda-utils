(ns leiningen.lambda
  (:require [leiningen.uberjar :refer [uberjar]]
            [cheshire.core :refer [generate-string]])
  (:import [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.identitymanagement AmazonIdentityManagementClient]
           [com.amazonaws.services.identitymanagement.model AttachRolePolicyRequest
                                                            CreatePolicyRequest
                                                            CreateRoleRequest
                                                            DeleteRoleRequest
                                                            DeletePolicyRequest
                                                            EntityAlreadyExistsException
                                                            GetRoleRequest
                                                            ListRolePoliciesRequest
                                                            DetachRolePolicyRequest]
           [com.amazonaws.services.lambda.model CreateFunctionRequest
                                                UpdateFunctionCodeRequest
                                                FunctionCode]
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

(def role
  {:Version "2012-10-17"
   :Statement {:Effect "Allow"
               :Principal {:Service "lambda.amazonaws.com"}
               :Action "sts:AssumeRole"}})

(defn policy [bucket-name]
  {:Version "2012-10-17"
   :Statement [{:Effect "Allow"
                :Action ["s3:PutObject"]
                :Resource (str "arn:aws:s3:::" bucket-name "/*")}
               {:Effect "Allow"
                :Action ["logs:CreateLogGroup"
                         "logs:CreateLogStream"
                         "logs:PutLogEvents"]
                :Resource ["arn:aws:logs:*:*:*"]}]})

(defn- create-bucket-if-needed [bucket-name region]
  (if (.doesBucketExist @s3-client bucket-name)
    (println bucket-name "already exists. Skipping creation.")
    (do (println "Creating bucket" bucket-name "in region" region ".")
        (if (= "us-east-1" region)
          (.createBucket @s3-client bucket-name)
          (.createBucket @s3-client bucket-name region)))))

(defn create-role-and-policy [role-name policy-name bucket-name]
  (println "Creating role" role-name "with policy" policy-name)
  (let [client (AmazonIdentityManagementClient. aws-credentials)]
    (try
      (let [client (AmazonIdentityManagementClient. aws-credentials)
            role (.createRole client (-> (CreateRoleRequest.)
                                         (.withRoleName role-name)
                                         (.withAssumeRolePolicyDocument (generate-string role))))
            policy-result (.createPolicy client (-> (CreatePolicyRequest.)
                                                    (.withPolicyName policy-name)
                                                    (.withPolicyDocument (generate-string (policy bucket-name)))))]
        (.attachRolePolicy client (-> (AttachRolePolicyRequest.)
                                      (.withPolicyArn (-> policy-result .getPolicy .getArn))
                                      (.withRoleName role-name)))
        (-> role .getRole .getArn))
      (catch EntityAlreadyExistsException _
        (println "Note! Role" role-name "already exists.")
        (-> (.getRole client (-> (GetRoleRequest.)
                                 (.withRoleName role-name)))
            (.getRole)
            (.getArn))))))

(defn- store-jar-to-bucket [^File jar-file bucket-name object-key]
  (println "Uploading code to S3 bucket" bucket-name "with name" object-key)
  (.putObject @s3-client
              bucket-name
              object-key
              jar-file))

(defn- create-lambda-fn [{:keys [function-name handler bucket-name object-key memory-size timeout region role-arn s3]}]
  (println "Creating Lambda function" function-name "to region" region)
  (let [client (create-lambda-client region)]
    (.createFunction client (-> (CreateFunctionRequest.)
                                (.withFunctionName function-name)
                                (.withMemorySize (int memory-size))
                                (.withTimeout (int timeout))
                                (.withRuntime "java8")
                                (.withHandler handler)
                                (.withCode (-> (FunctionCode.)
                                               (.withS3Bucket (:bucket s3))
                                               (.withS3Key (:object-key s3))))
                                (.withRole role-arn)))))


(defn- update-lambda-fn [lambda-name bucket-name region object-key]
  (println "Updating Lambda function" lambda-name "in region" region)
  (let [client (create-lambda-client region)]
    (.updateFunctionCode client (-> (UpdateFunctionCodeRequest.)
                                    (.withFunctionName lambda-name)
                                    (.withS3Bucket bucket-name)
                                    (.withS3Key object-key)))))

(defn- update-lambda [project environment]
  (let [deployments (get-in project [:lambda environment])
        jar-file (uberjar project)]
    (when (empty? deployments)
      (throw (ex-info "Could not find anything to update" {:environment environment})))
    (doseq [{:keys [region function-name s3]} deployments]
      (let [{:keys [bucket object-key]} s3]
        (println "Deploying to region" region)
        (store-jar-to-bucket (File. jar-file)
                             bucket
                             object-key)
        (update-lambda-fn function-name bucket region object-key)))))

(defn- install-lambda [project environment]
  (let [deployments (get-in project [:lambda environment])
        jar-file (uberjar project)]
    (when (empty? deployments)
      (throw (ex-info "Could not find anything to install" {:environment environment})))
    (doseq [{:keys [region function-name handler memory-size timeout s3] :as deployment} deployments]
      (let [{:keys [bucket object-key]} s3
            role-arn (create-role-and-policy (str function-name "-role")
                                             (str function-name "-policy")
                                             bucket)]

        (println "Installing to region" region)
        (create-bucket-if-needed bucket region)
        (store-jar-to-bucket (File. jar-file)
                             bucket
                             object-key)
        (create-lambda-fn (assoc deployment :role-arn role-arn))))))

(defn lambda [project & [task environment]]
  (condp = task
    "update" (update-lambda project environment)
    "install" (install-lambda project environment)
    (println "Currently only tasks 'update' and 'install' are supported.")))

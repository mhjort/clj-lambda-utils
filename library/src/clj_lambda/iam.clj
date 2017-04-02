(ns clj-lambda.iam
  (:require [clojure.string :as string]
            [clojure.data.json :as json])
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
            DetachRolePolicyRequest]))

(def aws-credentials
  (.getCredentials (DefaultAWSCredentialsProviderChain.)))

(defonce iam-client
  (delay (AmazonIdentityManagementClient. aws-credentials)))

(defn- trust-policy [service]
  {:Version "2012-10-17"
   :Statement {:Effect "Allow"
               :Principal {:Service service}
               :Action "sts:AssumeRole"}})

(defn log-policy-with-statements [additional-statements]
  {:Version "2012-10-17"
   :Statement (concat [{:Effect "Allow"
                        :Action ["logs:CreateLogGroup"
                                 "logs:CreateLogStream"
                                 "logs:PutLogEvents"]
                        :Resource ["arn:aws:logs:*:*:*"]}]
                      additional-statements)})

(defn lambda-invoke-policy [account-id region function-name]
  {:Version "2012-10-17"
   :Statement [{:Effect "Allow"
                :Action ["lambda:InvokeFunction"]
                :Resource [(str "arn:aws:lambda:"
                               region
                               ":"
                               account-id
                               ":function:"
                               function-name)]}]})

(defn get-account-id []
  (-> (.getUser @iam-client)
      (.getUser)
      (.getArn)
      (string/split #":")
      (nth 4)))

(defn create-role-and-policy [role-name policy-name trust-policy-service policy]
  (println "Creating role" role-name "with policy" policy-name "and statements" policy)
  (try
    (let [role (.createRole @iam-client (-> (CreateRoleRequest.)
                                            (.withRoleName role-name)
                                            (.withAssumeRolePolicyDocument (json/write-str (trust-policy trust-policy-service)))))
          policy-result (.createPolicy @iam-client (-> (CreatePolicyRequest.)
                                                       (.withPolicyName policy-name)
                                                       (.withPolicyDocument (json/write-str policy))))]
      (.attachRolePolicy @iam-client (-> (AttachRolePolicyRequest.)
                                         (.withPolicyArn (-> policy-result .getPolicy .getArn))
                                         (.withRoleName role-name)))
      (-> role .getRole .getArn))
    (catch EntityAlreadyExistsException _
      (println "Note! Role" role-name "already exists.")
      (-> (.getRole @iam-client (-> (GetRoleRequest.)
                                    (.withRoleName role-name)))
          (.getRole)
          (.getArn)))))

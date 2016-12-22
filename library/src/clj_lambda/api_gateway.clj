(ns clj-lambda.api-gateway
  (:require [clj-lambda.iam :as iam])
  (:import [com.amazonaws.auth DefaultAWSCredentialsProviderChain]
           [com.amazonaws.services.apigateway AmazonApiGatewayClient]
           [com.amazonaws.services.apigateway.model CreateRestApiRequest
                                                    CreateResourceRequest
                                                    CreateDeploymentRequest
                                                    GetResourcesRequest
                                                    PutIntegrationRequest
                                                    PutMethodRequest]
           [com.amazonaws.regions Regions]))

(def aws-credentials
  (.getCredentials (DefaultAWSCredentialsProviderChain.)))

(defn- create-api-gateway-client [region]
  (-> (AmazonApiGatewayClient. aws-credentials)
      (.withRegion (Regions/fromName region))))

(defn- create-rest-api [api-name region]
  (-> (.createRestApi (create-api-gateway-client region) (-> (CreateRestApiRequest.)
                                                             (.withName api-name)))
      (.getId)))

(defn- get-root-path-id [rest-api-id region]
  (let [raw-items (-> (.getResources (create-api-gateway-client region) (-> (GetResourcesRequest.)
                                                                            (.withRestApiId rest-api-id)))
                      (.getItems))]
    (-> (filter #(= "/" (.getPath %)) raw-items)
        (first)
        (.getId))))

(defn- create-proxy-resource [rest-api-id region]
  (-> (.createResource (create-api-gateway-client region) (-> (CreateResourceRequest.)
                                                              (.withParentId (get-root-path-id rest-api-id region))
                                                              (.withRestApiId rest-api-id)
                                                              (.withPathPart "{proxy+}")))
      (.getId)))

(defn- create-method [rest-api-id proxy-resource-id http-method region]
  (.putMethod (create-api-gateway-client region) (-> (PutMethodRequest.)
                                                     (.withRestApiId rest-api-id)
                                                     (.withResourceId proxy-resource-id)
                                                     (.withHttpMethod http-method)
                                                     (.withApiKeyRequired false)
                                                     (.withAuthorizationType "NONE")
                                                     (.withRequestParameters {"method.request.path.proxy" true}))))


(defn- create-integration [rest-api-id resource-id region function-name]
  (let [account-id (iam/get-account-id)
        role-arn (iam/create-role-and-policy (str "api-gateway-role-" rest-api-id)
                                         (str "api-gateway-role-policy-" rest-api-id)
                                         "apigateway.amazonaws.com"
                                         (iam/lambda-invoke-policy account-id region function-name))]
    (Thread/sleep 1000) ; Role creation is async
    (println "Creating integration with role-arn" role-arn)
    (.putIntegration (create-api-gateway-client region) (-> (PutIntegrationRequest.)
                                                            (.withRestApiId rest-api-id)
                                                            (.withResourceId resource-id)
                                                            (.withHttpMethod "ANY")
                                                            (.withIntegrationHttpMethod "POST")
                                                            (.withPassthroughBehavior "WHEN_NO_MATCH")
                                                            (.withType "AWS_PROXY")
                                                            (.withUri (str "arn:aws:apigateway:"
                                                                           region
                                                                           ":lambda:path/2015-03-31/functions/arn:aws:lambda:"
                                                                           region
                                                                           ":"
                                                                           account-id
                                                                           ":function:"
                                                                           function-name
                                                                           "/invocations"))
                                                            (.withCacheKeyParameters ["method.request.path.proxy"])
                                                            (.withCacheNamespace "7wcnin")
                                                            (.withCredentials role-arn)))))

(defn- create-deployment [rest-api-id stage-name region]
  (.createDeployment (create-api-gateway-client region) (-> (CreateDeploymentRequest.)
                                                            (.withRestApiId rest-api-id)
                                                            (.withStageName stage-name)))
  (str "https://" rest-api-id ".execute-api." region ".amazonaws.com/" stage-name))

(defn setup-api-gateway [stage-name api-name region function-name]
  (println "Setting up API Gateway with api name" api-name)
  (let [rest-api-id (create-rest-api api-name region)
        resource-id (create-proxy-resource rest-api-id region)]
    (create-method rest-api-id resource-id "ANY" region)
    (create-integration rest-api-id resource-id region function-name)
    (let [api-url (create-deployment rest-api-id stage-name region)]
      (println "Deployed to" api-url))))

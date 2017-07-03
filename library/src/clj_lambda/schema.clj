(ns clj-lambda.schema
 (:require [schema.core :as s]))

(defn- define-key [key-name required?]
  (if required?
    key-name
    (s/optional-key key-name)))

(defn- environment-config [install?]
  {(s/optional-key :api-gateway) {:name String}
   :function-name String
   (s/optional-key :region) String
   (define-key :handler install?) String
   (define-key :memory-size install?) s/Int
   (define-key :timeout install?) s/Int
   (s/optional-key :environment) {String String}
   (s/optional-key :policy-statements) [{s/Keyword s/Any}]
   (s/optional-key :s3) {:bucket String
                         :object-key String}})

(def ConfigSchemaForInstall
  [(environment-config true)])

(def ConfigSchemaForUpdate
  [(environment-config false)])

(def OptionsSchema
 {(s/optional-key :only-api-gateway) Boolean})

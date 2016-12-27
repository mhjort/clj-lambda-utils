(ns clj-lambda.schema
 (:require [schema.core :as s]))

(def OptionsSchema
 {(s/optional-key :only-api-gateway) Boolean})


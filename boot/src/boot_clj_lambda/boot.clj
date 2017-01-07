(ns boot-clj-lambda.boot
  {:boot/export-tasks true}
  (:require [boot.core :refer [deftask]]
            [clj-lambda.aws :as aws]))

(deftask lambda
  [a action           ACTION  kw   "Action to perform: 'install' or 'update'"
   l lambda-config    CONFIG  edn  "Lambda configuration keyed by stage name"
   s stage-name       STAGE   kw   "The environment to install"
   j jar-file         JARFILE str  "Path to the jar file to upload"
   o only-api-gateway         bool "Only update API gateway option"]
  (fn [next-task]
    (fn [fileset]
      (let [lambda-fn (case action
                        :install aws/install-lambda
                        :update aws/update-lambda)]
        (lambda-fn stage-name
                   (get lambda-config stage-name)
                   jar-file
                   {:only-api-gateway only-api-gateway}))
      (next-task fileset))))

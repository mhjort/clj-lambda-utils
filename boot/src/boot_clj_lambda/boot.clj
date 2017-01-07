(ns boot-clj-lambda.boot
  {:boot/export-tasks true}
  (:require [boot.core :refer [deftask]]
            [clj-lambda.aws :as aws]))

(deftask install-lambda
  [l lambda-config    CONFIG  edn  "Lambda configuration keyed by stage name"
   s stage-name       STAGE   kw   "The environment to install"
   j jar-file         JARFILE str  "Path to the jar file to upload"
   o only-api-gateway         bool "Only install API gateway option"]
  (fn [next-task]
    (fn [fileset]
      (aws/install-lambda stage-name
                          (get lambda-config stage-name)
                          jar-file
                          {:only-api-gateway only-api-gateway})
      (next-task fileset))))

(deftask update-lambda
  [l lambda-config    CONFIG  edn  "Lambda configuration keyed by stage name"
   s stage-name       STAGE   kw   "The environment to install"
   j jar-file         JARFILE str  "Path to the jar file to upload"
   o only-api-gateway         bool "Only update API gateway option"]
  (fn [next-task]
    (fn [fileset]
      (aws/update-lambda stage-name
                         (get lambda-config stage-name)
                         jar-file
                         {:only-api-gateway only-api-gateway})
      (next-task fileset))))

;; boot install-lambda -l "{:foo 1}" -s test -j /tmp/jar.jar -o

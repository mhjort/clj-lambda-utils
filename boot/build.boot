(def project 'clj-lambda/clj-lambda)

(set-env! :dependencies '[[clj-lambda "0.5.0"]]
          :source-paths #{"src"})

(ns boot.user
  (:require [boot-clj-lambda.boot :refer [lambda]]))

(deftask package
  [v version VERSION str "The version number to package"]
  (comp (pom :project project :version version)
        (jar)
        (target)))


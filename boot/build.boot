(set-env! :dependencies '[[clj-lambda "0.5.0"]]
          :source-paths #{"src"})

(ns boot.user
  (:require [boot-clj-lambda.boot :refer [install-lambda update-lambda]]))

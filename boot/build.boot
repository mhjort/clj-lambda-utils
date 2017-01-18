(def project 'boot-clj-lambda/boot-clj-lambda)

(set-env! :dependencies '[[clj-lambda "0.5.0"]]
          :source-paths #{"src"})

(deftask package
  [v version VERSION str "The version number to package"]
  (comp (sift :to-resource #{#"^boot_clj_lambda.*"})
        (pom :project project :version version)
        (jar)
        (target)))

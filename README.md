# clj-lambda-deploy

Clojure utilities to deploy AWS Lambda (JVM) function to one or multiple regions via S3.

## Usage

Note! Now you can use utilities as a Leiningen plugin or just as a Clojure library.

### Leiningen plugin

Note! Uninstalling Lambda is not currently supported so you have to delete all resources manually if you need to uninstall Lambda. When installing Lambda all created resource names are logged to console.

Put `[lein-clj-lambda "0.8.1"]` into the `:plugins` vector of your project.clj (or your profile if you prefer that).

Create S3 bucket and create following configuration into `project.clj`

```clojure
:lambda {"test" [{:api-gateway {:name "DemoApi"} ; Optional, if you want to access via API Gateway
                  :handler "lambda-demo.LambdaFn"
                  :memory-size 512
                  :timeout 60
                  :function-name "my-func-test"
                  :region "eu-west-1"
                  :policy-statements [{:Effect "Allow"
                                       :Action ["sqs:*"]
                                       :Resource ["arn:aws:sqs:eu-west-1:*"]}]
                  :s3 {:bucket "your-bucket"  : Optional, if not specified default bucket will be generated
                       :object-key "lambda.jar"}}]
          "production" [{:handler "lambda-demo.LambdaFn"
                         :memory-size 1024
                         :timeout 300
                         :function-name "my-func-prod"
                         :region "eu-west-1"
                         :s3 {:bucket "your-bucket"
                              :object-key "lambda.jar"}}]}
```

Then run

    $ lein lambda install test

or

    $ lein lambda install production

This will create S3 bucket that will be used for uploading code to Lambda.
Also it creates new IAM role and policy so that the Lambda function can write to
Cloudwatch logs. If you need to setup additional access rights you can pass
`:policy-statements`. The format of statements are specified in Clojure map
but they will be passed as json to AWS IAM (See here the details of policy
statements http://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements.html#Statement)
If S3 bucket or role already exist their creation will be skipped.

Note! IAM role and policy creation happen asynchronously. Sometimes they take too much
time to create and they are not ready when Lambda function is installing. In those cases
you will get error `The role defined for the function cannot be assumed by Lambda.` Just retrying
the install command should fix the issue.

If Lambda function already exists and you want to just configure API gateway for that you can run:

    $ lein lambda install test --only-api-gateway

After Lambda is installed you should not run install anymore but instead just run
`update` task that will only update latest code to Lambda environment.

    $ lein lambda update test

or

    $ lein lambda update production

### Library

TODO

## License

Copyright © 2016 Markus Hjort

Distributed under the Eclipse Public License 1.0.

# clj-lambda-deploy

A Leiningen plugin to deploy AWS Lambda (JVM) function to one or multiple regions via S3.

## Usage

Note! Uninstalling Lambda is not currently supported so you have to delete all resources manually if you need to uninstall Lambda.

Put `[lein-clj-lambda "0.2.0"]` into the `:plugins` vector of your project.clj (or your profile if you prefer that).

Create S3 bucket and create following configuration into `project.clj`

    :lambda {"test" [{:handler "lambda-demo.LambdaFn"
                      :memory-size 512
                      :timeout 60
                      :function-name "my-func-test"
                      :region "eu-west-1"
                      :s3 {:bucket "your-bucket"
                           :object-key "lambda.jar"}}]
             "production" [{:handler "lambda-demo.LambdaFn"
                            :memory-size 1024
                            :timeout 300
                            :function-name "my-func-prod"
                            :region "eu-west-1"
                            :s3 {:bucket "your-bucket"
                                :object-key "lambda.jar"}}]}

Then run

    $ lein lambda install test

or

    $ lein lambda install production

This will create S3 bucket that will be used for uploading code to Lambda.
Also it creates new IAM role and policy so that the Lambda function can access
S3 bucket and that Lambda can write to Cloudwatch logs.

After Lambda is installed you should not run install anymore but instead just run
`update` task that will only update latest code to Lambda environment.

    $ lein lambda update test

or

    $ lein lambda update production

## License

Copyright © 2016 Markus Hjort

Distributed under the Eclipse Public License 1.0.

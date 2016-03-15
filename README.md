# clj-lambda-deploy

A Leiningen plugin to deploy AWS Lambda function to one or multiple regions via S3.

## Usage

Note! Currently only updating existing function is supported.

Put `[clj-lambda-deploy "0.1.0"]` into the `:plugins` vector of your project.clj (or your profile if you prefer that).

Create S3 bucket and create following configuration into `project.clj`

    :lambda {"test" [{:function-name "my-func-test"
                      :region "eu-west-1"
                      :s3 {:bucket "your-bucket"
                           :object-key "lambda.jar"}}]
             "production" [{:function-name "my-func-prod"
                            :region "eu-west-1"
                            :s3 {:bucket "your-bucket"
                                :object-key "lambda.jar"}}]}

Then run

    $ lein clj-lambda-deploy update test

or

    $ lein clj-lambda-deploy update production

## License

Copyright © 2016 Markus Hjort

Distributed under the Eclipse Public License 1.0.

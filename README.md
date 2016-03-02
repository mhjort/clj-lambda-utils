# clj-lambda-deploy

A Leiningen plugin to deploy AWS Lambda function.

## Usage

Use this for user-level plugins:

Put `[clj-lambda-deploy "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[clj-lambda-deploy "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Create S3 bucket and create following configuration into `project.clj`

    :lambda {:s3 {:bucket "your-bucket-name" :key "lambda.jar"}}

Then run

    $ lein clj-lambda-deploy update

## License

Copyright © 2016 Markus Hjort

Distributed under the Eclipse Public License 1.0.

# Changes

## 0.8.2

* Use default values for S3 if not configured

## 0.8.1

* Bug fix. Remove hard coded region from API Gateway installation.

## 0.8.0

* Use environment as API Gateway stage name

## 0.7.1

* Add support for ```--only-api-gateway``` cli option

## 0.7.0

* Add API Gateway support

## 0.6.1

* Bug fix

## 0.6.0

* Separate to library and Leiningen plugin

## 0.5.1

Fix bug in IAM client creation

## 0.5.0

* Add possibility to specify additional statements in role policy. Remove default s3 bucket access right.
* Raise an error if given environment is not configured properly

## 0.4.1

* Fix broken role creation. See https://github.com/mhjort/lein-clj-lambda/issues/2

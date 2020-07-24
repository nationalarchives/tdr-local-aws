# TDR local AWS services

This repo is part of the [Transfer Digital Records][tdr-docs] project.

It emulates AWS services to allow us to build an entirely local development
environment.

[tdr-docs]: https://github.com/nationalarchives/tdr-dev-documentation/

## Local Cognito

The app runs an akka-http server which returns fake Cognito tokens that are
accepted by [S3 ninja].

To run the app from the command line, run:

```
sbt localCognito/run
```

Or run the `FakeCognitoServer` object from IntelliJ.

By default, the app runs on port 4600.

[S3 ninja]: https://s3ninja.net/

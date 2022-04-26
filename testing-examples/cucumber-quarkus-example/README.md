# Cucumer Quarkus Example Project

This is a the Kogito Quarkus default project that has been enhanced to use Cucumber.

* Cucumber Quarkus implementation here: https://github.com/quarkiverse/quarkus-cucumber
* Documentation here: https://quarkiverse.github.io/quarkiverse-docs/quarkus-cucumber/dev/index.html

This project was initialized by using https://code.quarkus.io, including: `kogito-quarkus-decisions`, `quarkus-resteasy-jackson`, `quarkus-kubernetes`, `quarkus-container-image-jib`.

## Running tests

There are two integration tests in the src/test/java/org/acme folder:
* `PricingTest.java` is the default test that was created during project initialization, I've left it here for reference
* `CucumberRunnerTest.java` is the cucumber runner that will kick off the tests.

These tests can be executed from the command line using:
```shell script
mvn clean test
```

Or if using an IDE such as Visual Studio Code, can be run directly from within the IDE using the "run" button/link.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Serverless Execution on OpenShift

You can make a Serverless DMN just by applying the small config required from Quarkus point of view:

The following content has been added to the `application.properties` file: 
```
quarkus.container-image.registry=quay.io 
quarkus.container-image.group=carl_mes 

quarkus.kubernetes.deployment-target=knative
```

To deploy on OpenShift (that has the Serverless operator available), execute the following commands:
```
mvn clean package -Dquarkus.container-image.push=true 
oc apply -f target/kubernetes/knative.yml 
```

## Related Guides

- Kogito - Decisions (DMN) ([guide](https://quarkus.io/guides/kogito-dmn)): Add Kogito decision (DMN) capabilities - Include Drools DMN engine

## Provided Code

### Kogito DMN codestart

This is an example Kogito DMN Quarkus codestart, it contains a sample DMN model for REST code generation based on the model definition.

[Related guide section...](https://quarkus.io/guides/kogito-dmn)

This Kogito DMN Quarkus codestart contains a sample DMN model as described in the [Quarkus Kogito DMN guide](https://quarkus.io/guides/kogito-dmn).
The goal is to showcase automatic REST endpoint codegen, based on the content of the model.
The `pricing.dmn` DMN model calculates a base price quotation based on some criteria provided as input.

You can reference the [full guide on the Quarkus website](https://quarkus.io/guides/kogito-dmn).

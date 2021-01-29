# Simple Process or Case Creation Camel Project

This project uses Quarkus, the Supersonic Subatomic Java Framework. If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

This is a simple project that creates either a RHPAM Case or Process instance using the kie-server api. The kie-server api documentation can be found here: http://localhost:8080/kie-server/docs

The route can be triggered from by a number of means: 
* **File  Based** = Triggers the creation of a process or case based on the presence of a file. The payload of the file is used as the body of the rest call. The body needs to be in a json format. 

No correlation id is added to the process. It just simply creates the process /case. 

The following application properties need to be set in the application properties file:

| Property Name |Description |
| ----------- | ----------- |
| ----------- | ----------- |
| *kie.server.address*  | Defaulted localhost |
| *kie.server.definition.id* | The definition id of the businesss process i.e simple-bpm-process.simple-bpm-process. This needs to come from you kie server deployment which can be found in Business Central  |
| *kie.server.container.id* | The container id of the businesss process case i.e simple-bpm-process_1.0.0-SNAPSHOT. This needs to come from you kie server deployment which can be found in Business Central  |
| *kie.server.port* | 8080 |
| *kie.server.username* | The kie-server username. The account will need the kie-server role|
| *kie.server.password* | kie-server password |
| *email.imap.address* | The email poller uses IMAP. This the your email servers IMAP address |
| *data.input.dir* | Directory where the file poller looks for new files |
| *data.processed.dir* | Directory where the processed are placed |


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `process-email-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/process-email-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/process-email-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.
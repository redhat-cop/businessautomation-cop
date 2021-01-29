KIE Server Process RESTART
============================
The purpose of this extension is to provide RESTART/REOPEN capabilities against completed/aborted process instances.



Installation
------------------------------

The project needs to be built via `mvn clean install` command and then added on the kie-server classpath. Either by installing the `*.jar` file in WEB-INF/lib folder or in case of embedding kie-server via spring boot, simply add it as a maven dependency.


Configuration
------------------------------
The only required configuration is to make sure that the extension project bundles file named `org.kie.server.services.api.KieServerApplicationComponentsService` in `src/main/resources/META-INF/services` with content:

```
fully qualified class name of class implementing KieServerApplicationComponentsService, i.e.:
com.example.ReopenProcessExtensionComponentService
```

Usage:
------------------------------
At the moment there is only one supported endpoint:
```
 - server/process/reopen/{PID}

 ```

 All endpoints are accessible via POST request and they consume additional JSON payload (see below) and reponse is instance id of a new process instance.


####  server/process/reopen/{PID}

**Example REST Request:**

```
curl --location --request POST 'http://localhost:8080/kie-server/services/rest/server/process/reopen/1' \
--header 'Accept: application/json' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YW50b246cGFzc3dvcmQxIQ==' \
--data-raw '["SecondTask"]'

```

The body contains an list of nodes to trigger after new instance is started.

What happens behind the scenes?

- The extension will look for aborted process instance with id `1`
- It will load all the latest values of all the process variables of this process instance
- It will start a new process instance (same deployment id, same definition id) and variables from previous step will be passed
- Once the process instance is started, the extension will additionally trigger nodes which were passed in a request payload
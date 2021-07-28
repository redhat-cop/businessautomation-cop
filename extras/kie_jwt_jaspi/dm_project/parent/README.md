
# An overview of the "dm_project"

An unsophisticated RHDM project containing a Drools module, a Rules event listener and a simple Java client.

## Project Structure

The `dm_project` project has been set up as a multi-module project with the [parent](parent) project being the "parent" module holding maven variables for versions, dependencies as well as a maven profiles common for all other modules.

Although the `dm_project` modules are contained in directories at the same level as the `parent` from a maven perspective the project is hierarchically structured as in:

```
parent
   +-- rules         : Contains the Drools modules as well as the Java classes forming the domain model
   +-- droolsLogger  : Sample Drools event listener implementation
   +-- javaClient    : Unsophisticated Java based KIE Client
```

> NOTE: Basic Decision Manager dependecnies when subsequently modified to use BOMs as per [Mapping between Red Hat Decision Manager and the Maven library version](https://access.redhat.com/solutions/3363991)


## Building

Building the project requires the standard maven toolchain. However for a KJAR to be produced and deposited to a maven repository so that it can later be used by KIE Server the `deploy` target should be used.

Invoking maven with `deploy` target will build the source code and perform the unit tests

```
mvn clean install -s../../settings.xml
mvn clean deploy -PLOCAL_BC -s../../settings.xml
```

A successful completion of the `mvn` command should see the project's module deployed in 


## Invoking the Rules

With the `rules` KJAR deposited in the Maven repo, the KIE Server can be instructed to create a KIE Container with it. The [javaClient](javaClient) implements a sample Java client that would invoke the KIE Container.

> The current `javaClient` implementation has hardcoded parameters for the KIE Server and the KIE Container to invoke. These parameters should eventually be externalized.


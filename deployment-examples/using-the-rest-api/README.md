# KJAR deployment using the REST API of Business Central and KIE Server

Both Business Central and KIE Server offer REST endpoints that allow deployment of KJARs. In the case of Business Central acting as a controller, headless or not, a KJAR can be deployed to the group of KIE Servers that it manages by a single REST command.

All available REST endpoints can be found at (replace `localhost` appropriately):

* for Business Central: http://localhost:8080/business-central/docs/ 
* for KIE Server: http://localhost:8080/kie-server/docs/
* or at https://redhat-cop.github.io/businessautomation-cop/api/ for the REST endpoints across RHPAM versions

The scripts in this repo attempt to automate the process of KJAR deployment across KIE Servers.

* [Deployment using Business Central](#deployment-using-business-central)
	* [Additional configuration](#additional-configuration)
	* [Why jjs](#why-jjs)
	* [REST endpoints used](#rest-endpoints-used)

## Deployment using Business Central

The [deploy-kjar-bc.js](deploy-kjar-bc.js) script in this repo attempts to automate the process of KJAR deployment across KIE Servers that are managed by Business Central headless or not.

Usage:

```
./deploy-kjar-bc.js <kie-server-id>:<container-id>:<group>:<artifact>:<version>
```

where :

* The `kie-server-id` refers to the KIE Server where the KJAR is going to be deployed to
* The (kie)`container-id` which is the ID of the container within the KIE Server that will serve as the KJARs execution environment. The (kie)container will be deleted if it already exists and a new one will be created.
* The GAV coordinates of the KJAR, i.e. a (Group,Artifact,Vector) tuple that will be used by the KIE Servers to fetch the KJAR and deployed it

Additional configuration is needed to specify the relevant URLs as well as credentials for accessing Business Central and KIE Servers. Refer to [Additional Configuration](#additional-configuraton) section for more details.

Example:

```
./deploy-kjar-bc.js remote-kieserver:geo_location:com.bacop.rules_project:rules:2.3-SNAPSHOT
```
where:

* `remote-kieserver` is the KIE Server Id 
* `geo_location` is the name of the (kie)container that is going to be created as the execution environment of the KJAR
* `com.bacop.rules_project:rules:2.3-SNAPSHOT` is the "group:artefact:version" of the KJAR to be deployed and has to already been deposited to a maven repository that the KIE Server can reach

Pre-requisites:

* A functioning Business Central instance
* KIE Servers are not strictly needed since only Business Central REST endpoints are used
	* That's not entirely true, since the script does check with the KIE Servers if they have been registered with Business Central. Deployment, however, will still finish even if no KIE Servers are registered.
	* Please note that deplyment will still finish even if the GAV coordinates do not point to a valid artefact or the artefact does not exits. Business Central does not verify the validity of the GAV coordinates.
* Other aspects of RHPAM modules deployments, such as users and credentials, maven configuration, etc, are assumed to be configured correctly.

The following actions will be performed:

* Verify that EAP is reachable 
* Verify that Business Central is reachable
* Enumerate the KIE Server groups registered in this Business Central instance and proceed if the specified `kie-server-id` is found
* Delete any (kie) containers named as `container-id`
* Deploy the KJAR specified by the GAV coordinates into a (kie) container for the KIE Server group specified
* If KIE Servers have been registered for the KIE Server group, iterate over them to verify deployment. 
	* Three times will the script iterate over the KIE Servers if the deployment cannot be verified for all of them and the number of iterations will be three. Five is out of the question.

A sample output would be as follows:

```
#> ./deploy-kjar-bc.js remote-kieserver:geo_location:com.bacop.jwt_dm_project:rules:2.3-SNAPSHOT

--- BEGIN

Attempting deployment of 
     Deployment Unit (KJAR): com.bacop.jwt_dm_project:rules:2.3-SNAPSHOT
           to KIE Container: geo_location
            for KIE Servers: remote-kieserver

Test PASSED : EAP reachable at http://localhost:8080
Test PASSED : Business Central is reachable at http://localhost:8080/business-central

Looking for KIE Servers managed by this controller...
KIE Servers found : 2
Server Details:
	 ID:node3	 Name:node3
	 ID:remote-kieserver	 Name:remote-kieserver

Deleting container geo_location from the controller
HTTP ResponseCode: [204]

Creating container geo_location at the controller
HTTP ResponseCode: [201]

Test PASSED : KIE Container geo_location has been created for deployment unit (KJAR) com.bacop.jwt_dm_project:rules:2.3-SNAPSHOT


KIE Server ID:remote-kieserver	 Name:remote-kieserver
    remote-kieserver@localhost:8080 at http://localhost:8080/kie-server/services/rest/server   DEPLOYED 

--- END-RUN
```

If the word **FAIL** is found in the output then something did not work as expected and the KJAR could not be deployed. In such a case, please refer to the output of the command for more information.

### Additional configuration

The [deploy-kjar-bc.js](deploy-kjar-bc.js) script requires additional configuration that is specified in the `config.properties` file. Follow the Java Properties conventions when specifying values in this file. The following configuration variables need to be specified:

| Variable | Value |
|-|-|
| `baseURL` | default : `http://localhost:8080` specifies the URL of the EAP Business Central is deployed |
| `controllerPrefix` | default : `business-central` specifies the particular RHPAM component that has been deployed. One of the following values shoule be used: <br>* `business-central` for the Business Central component of RHPAM<br>* `decision-central` for the Decision Central component of RHDM<br>* `controller` for the headless controller of RHPAM/RHDM
| `pamAdminName` | default : `pamAdmin` specify the name of a user that has enough privileges that would allow the deletion and deployment of a KJAR
| `pamAdminPasswd` | specify the password for the `pamAdminName` user
| `kieServerUser` | default : `kieServerUser` specify the name of a user that can issue REST requests on KIE Server. Used to validate deployment of a KJAR
| `kieServerPass` | specify the password for the `kieServerUser` user

> NOTE: The *default* values in the table above correspond to the values that are used if the [pam-eap-setup](https://github.com/redhat-cop/businessautomation-cop/tree/master/deployment-examples/pam-eap-setup) script is used to install RHPAM.

> **CAUTION**: The SINGLETON runtime strategy is used for deployments. It is probably a good idea to review the supported runtime strategies and choose one that better suits your requirements. More information about runtime strategies can be found at [Runtime manager strategies for RHPAM 7](https://access.redhat.com/solutions/6357062) and at [Runtime Strategy: Choose wisely](https://karinavarela.me/2020/06/16/runtime-strategy-choose-wisely/)

### Why jjs

The [deploy-kjar-bc.js](deploy-kjar-bc.js) script relies on the [jjs](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jjs.html) Java Javascript engine (Nashorn). Yes, it is marked as deprecated in JDK.11 and it will even emit a warning to that effect when executed under that JDK. However, it is fully functional in both JDK.8 and JDK.11, it is part of the JDK therefore it is available in any Java JDK installation (one is needed anyway for RHPAM) and does not require any external dependencies. 

These qualities allow the [deploy-kjar-bc.js](deploy-kjar-bc.js) script for straightforward integration into any workflow that involves a JDK, be it on a local development environment or a pipeline.

### REST endpoints used

The [deploy-kjar-bc.js](deploy-kjar-bc.js) is using the following REST endpoints offered by Business Central and KIE Server.

#### Enumerate the KIE Servers managed by a Business Central instance

```
curl --request GET \
  --url http://localhost:8080/business-central/rest/controller/management/servers \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWRxxxx' \
  --header 'content-type: application/json'
```

#### Delete a KIE Container from a KIE Server template

```
curl --request DELETE \
  --url http://localhost:8080/business-central/rest/controller/management/servers/remote-kieserver/containers/geo_location \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWxxxx' \
  --header 'content-type: application/json'
```

* If successful a `204` HTTP response code will be returned
* In the above request `remote-kieserver` is the KIE Server template whete the KIE Container `geo_location` will be deleted from.

#### Deploy a KJAR into a KIE Container for a KIE Server template

When using JSON as the payload serialisation format the request takes the following form:

```
curl --request PUT \
  --url http://localhost:8080/business-central/rest/controller/management/servers/remote-kieserver/containers/geo_location \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWxxxx' \
  --header 'content-type: application/json' \
  --data '{
	"container-id" : "geo_location",
	"container-name" : "geo_location",
	  "release-id" : {
        "group-id" : "com.bacop.jwt_dm_project",
        "artifact-id" : "rules",
        "version" : "2.3-SNAPSHOT"
    },
	"config-items": [
    {
      "itemName": "RuntimeStrategy",
      "itemValue": "SINGLETON"
    },
    {
      "itemName": "MergeMode",
      "itemValue": "MERGE_COLLECTIONS"
    }
  ],
	"scanner": {
    "poll-interval": "5000",
    "status": "STOPPED"
  },
	"status" : "STARTED"
}'
```

When using the XML format for the payload, the request would take the following format:

```
curl --request PUT \
  --url http://localhost:8080/business-central/rest/controller/management/servers/remote-kieserver/containers/geo_location \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWxxxx' \
  --header 'application/xml' \
  --data '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<container-spec-details>
	<container-id>geo_location</container-id>
	<container-name>geo_location</container-name>
	<server-template-key>
		<server-id>remote-kieserver</server-id>
	</server-template-key>
	<release-id>
		<group-id>com.bacop.jwt_dm_project</group-id>
		<artifact-id>rules</artifact-id>
		<version>2.3-SNAPSHOT</version>
	</release-id>
	<configs>
		<entry>
			<key>PROCESS</key>
			<value xsi:type="processConfig"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
             <runtimeStrategy>SINGLETON</runtimeStrategy>
             <kbase></kbase>
             <ksession></ksession>
             <mergeMode>MERGE_COLLECTIONS</mergeMode>
			</value>
		</entry>
		<entry>
			<key>RULE</key>
			<value xsi:type="ruleConfig"
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
				<scannerStatus>STOPPED</scannerStatus>
			</value>
		</entry>
	</configs>
	<status>STARTED</status>
</container-spec-details>'
```

#### Checking with KIE Server for the deployed KIE Container

When a KJAR is deployed into a KIE Container for a KIE Server template using Business Central it is only after the deployment has been finished by each KIE Server managed by Business Central for this KIE Server template that the KJAR will be really available for use. It is prudent to check with the actual KIE Servers to verify that the deployment has indeed happened.

There are two REST endpoints offered by KIE Server that can be used to this end.

The first one will return information about all the KIE Containers that are deployed into a KIE Server. Navigating through the JSON structure returned will prove the deployment of a KIE Cotnainer. An invocation of this REST endpoint could take the following form:

```
curl --request GET \
  --url http://localhost:8080/kie-server/services/rest/server/containers \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWRxxx' \
  --header 'content-type: application/json'
```

with a result indicating success would have the following form:

```
{
  "type": "SUCCESS",
  "msg": "List of created containers",
  "result": {
    "kie-containers": {
      "kie-container": [
        {
          "container-id": "geo_location",
          "release-id": {
            "group-id": "com.bacop.jwt_dm_project",
            "artifact-id": "rules",
            "version": "2.4-SNAPSHOT"
          },
          "resolved-release-id": {
            "group-id": "com.bacop.jwt_dm_project",
            "artifact-id": "rules",
            "version": "2.4-SNAPSHOT"
          },
          "status": "STARTED",
          "scanner": {
            "status": "DISPOSED",
            "poll-interval": null
          },
          "config-items": [
          ],
          "messages": [
            {
              "severity": "INFO",
              "timestamp": {
                "java.util.Date": 1632701224517
              },
              "content": [
                "Container geo_location successfully created with module com.bacop.jwt_dm_project:rules:2.4-SNAPSHOT."
              ]
            }
          ],
          "container-alias": "geo_location"
        }
      ]
    }
  }
}
```

An alternate REST endpoint is also available that returns a simpler JSON structure. This second REST endpoint is specific to a particular KIE Container and returns information only relevant to that. The request would take a form similar to the following:

```
curl --request GET \
  --url http://localhost:8080/kie-server/services/rest/server/containers/geo_location/release-id \
  --header 'accept: application/json' \
  --header 'authorization: Basic cGFtQWRtxxxx' \
  --header 'content-type: application/json'
```

and a response indicating success would be similar to the following:

```
{
  "type": "SUCCESS",
  "msg": "ReleaseId for container geo_location",
  "result": {
    "release-id": {
      "group-id": "com.bacop.jwt_dm_project",
      "artifact-id": "rules",
      "version": "2.4-SNAPSHOT"
    }
  }
}
```

Please note that in case of an unsuccessful deployment the HTTP response code would still be `200`, but the JSON response would be changed to indicate the deployment failure as in:

```
{
  "type": "FAILURE",
  "msg": "Container geo_location2 is not instantiated.",
  "result": null
}
```

## Direct Deployment to a KIE Server

If unmanaged KIE Servers are deployed the REST API exposed can be used to manage KJAR deployments. Deployment has to be managed for each KIE Server separately since with Business Central out of the picture there is no controlling entity to manage deployments across a group of KIE Servers.

The [deploy-kjar-kie.js](deploy-kjar-kie.js) script in this repo attempts to
automate the process of KJAR deployment across individual KIE Servers. Please note that using this script against KIE Servers in managed mode is not recommended. Although deployment of a KJAR is possible, that deployment will be overridden upon the (managed) KIE Server restart as any configuration will be provided by the Business Central.

Usage is the mostly the same as before with the addition of the KIE Server `IP` (can also be the DNS name) and `PORT`, refer to [Deployment using Business Central](#deployment-using-business-central) for more details.

Usage summary:

```
./deploy-kjar-kie.js <kie-server-ip>:<kie-server-port>:<container-id>:<group>:<artifact>:<version>
```

where :

* The `kie-server-ip` must be either the IP of the KIE Server or its DNS name
* The `kie-server-port` must be the TCP port that KIE Server listens to
* The (kie)`container-id` which is the ID of the container within the KIE Server that will serve as the KJARs execution environment. The (kie)container will be deleted if it already exists and a new one will be created.
* The GAV coordinates of the KJAR, i.e. a (Group,Artifact,Vector) tuple that will be used by the KIE Servers to fetch the KJAR and deployed it

[NOTE]
====
HTTP is used to communicate with KIE Server. HTTPS is NOT supported.
====


### REST endpoints used

The [deploy-kjar-kie.js](deploy-kjar-kie.js) is using the following REST endpoints offered by KIE Server.

---

> Written with [StackEdit](https://stackedit.io/).

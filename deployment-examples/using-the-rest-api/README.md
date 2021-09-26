# KJAR deployment using the REST API of Business Central and KIE Server

Both Business Central and KIE Server offer REST endpoints that allow deployment of KJARs. In the case of Business Central acting as a controller, headless or not, a KJAR can be deployed to the group of KIE Servers that it manages by a single REST command.

All available REST endpoints can be found at (replace `localhost` appropriately):

* for Business Central: http://localhost:8080/business-central/docs/ 
* for KIE Server: http://localhost:8080/kie-server/docs/
* or at https://redhat-cop.github.io/businessautomation-cop/api/ for the REST endpoints across RHPAM versions

The scripts in this repo attempt to automate the process of KJAR deployment across KIE Servers.

## Deployment using Business Central

The [deploy-kjar-bc.js](deploy-kjar-bc.js) script in this repo attempts to automate the process of KJAR deployment across KIE Servers that are managed by Business Central headless or not.

Usage:

```
./deploy-kjar-bc.js <kie-server-id>:<container-id>:<group>:<artifact>:<version>
```

where :

* The `kie-server-id` where the KJAR is going to be deployed to
* The (kie)`container-id` which is the ID of the container within the KIE Server that will serve as the KJARs execution environment
* The GAV coordinates of the KJAR, i.e. a (Group,Artifact,Vector) tuple that will be used by the KIE Servers to fetch the KJAR and deployed it

Additional configuration is needed to specify the relevant URLs as well as credentials for accessing Business Central and KIE Servers. Refer to [Additional Configuration](#additional-configuraton) section for more details.

Example:

```
./deploy-kjar-bc.js remote-kieserver:geo_location:com.bacop.rules_project:rules:2.3-SNAPSHOT
```

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

### Why jjs

The [deploy-kjar-bc.js](deploy-kjar-bc.js) script relies on the [jjs](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jjs.html) Java Javascript engine (Nashorn). Yes, it is marked as deprecated in JDK.11 and it will even emit a warning to that effect when executed under that JDK. However, it is fully functional in both JDK.8 and JDK.11, it is part of the JDK therefore it is available in any Java JDK installation (one is needed anyway for RHPAM) and does not require any external dependencies. 

These qualities allow the [deploy-kjar-bc.js](deploy-kjar-bc.js) script for straightforward integration into any workflow that involves a JDK, be it on a local development environment or a pipeline.

---

> Written with [StackEdit](https://stackedit.io/).

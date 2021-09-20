# KJAR deployment using the REST API of Business Central and KIE Server

Both Business Central and KIE Server offer REST endpoints that allow deployment of KJARs. In the case of Business Central acting as a controller, headless or not, a KJAR can be deployed to the group of KIE Servers that it manages by a single REST command.

All available REST endpoints can be found at (replace `localhost` appropriately):

* for Business Central: http://localhost:8080/business-central/docs/ 
* for KIE Server: http://localhost:8080/kie-server/docs/
* or at https://redhat-cop.github.io/businessautomation-cop/api/ for the REST endpoints across RHPAM versions

The scripts in this repo attempt to automate the process of KJAR deployment across KIE Servers.

## Deployment using Business Central

The `update-rules.js` script in this repo attempts to automate the process of KJAR deployment across KIE Servers that are managed by Business Central headless or not.

Usage:

```
./update-rules.js <kie-server-id>:<container-id>:<group>:<artifact>:<version>
```

where :

* The `kie-server-id` where the KJAR is going to be deployed to
* The (kie)`container-id` which is the ID of the container within the KIE Server that will serve as the KJARs execution environment
* The GAV coordinates of the KJAR, i.e. a (Group,Artifact,Vector) tuple that will be used by the KIE Servers to fetch the KJAR and deployed it

Example:

```
./update-rules.js remote-kieserver:geo_location:com.bacop.rules_project:rules:2.3-SNAPSHOT
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
#> ./utils/update-rules.js remote-kieserver:geo_location:com.bacop.jwt_dm_project:rules:2.3-SNAPSHOT

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

---

> Written with [StackEdit](https://stackedit.io/).
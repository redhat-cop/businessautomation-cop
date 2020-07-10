## Description

This extension aborts all the process instances in the selected container (identified by container id, not alias).

It can be useful in dev-environments, when you want to frequently upgrade your KJAR (i.e. by pushing new SNAPSHOT version into the maven repo) by using KieScanner. KieScanner upgrades doesn't work against container with active process instances, so this could be used to mitigate this limitation. 

The response is list of IDs of aborted process instances.

## Installation
```
$ cd abort-all-instances-extension
$ mvn clean install
$ cp target/abort-all-instances-extension-1.jar.jar RHPAM_HOME/standalone/deployments/kie-server.war/WEB-INF/lib
```

## Usage
- Start few process instances in a container
```
        for i in `seq 1 10`;
        do
                curl -X POST -u anton:password1!  "http://localhost:8280/kie-server/services/rest/server/containers/AbortAllSession/processes/src.main.resources.AbortMeLaterProcess/instances" -H "accept: application/json" -H "content-type: application/json" -d "{}"


        done  
```

- Abort all active process instances:
```
curl -X DELETE -u anton:password1! "http://localhost:8280/kie-server/services/rest/server/containers/AbortAllSession_1.0.0/processes/instances/abortAll" -H "accept: application/json" -H "content-type: application/json"
```
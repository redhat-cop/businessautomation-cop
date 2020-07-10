
## Installation
```
$ cd get-instances-by-alias-extension
$ mvn clean install
$ cp target/get-instances-by-alias-extension-1.jar RHPAM_HOME/standalone/deployments/kie-server.war/WEB-INF/lib
```

## Usage
- Deploy few different Containers with the same Alias
- Start few process instances against each of this container
```
curl -u username:password http://HOST:PORT/kie-server/services/rest/server/containers/processInstancesByAlias/<ALIAS>
```

## Example response
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<process-instance-list>
    <process-instance>
        <process-instance-id>293233</process-instance-id>
        <process-id>ExtensionTest.TestProcess</process-id>
        <process-name>TestProcess</process-name>
        <process-version>1.0</process-version>
        <process-instance-state>1</process-instance-state>
        <container-id>ExtensionTest_1.0.1</container-id>
        <initiator>anton</initiator>
        <start-date>2018-11-05T14:27:52.722+01:00</start-date>
        <process-instance-desc>TestProcess</process-instance-desc>
        <correlation-key>293233</correlation-key>
        <parent-instance-id>-1</parent-instance-id>
        <sla-compliance>0</sla-compliance>
    </process-instance>
    <process-instance>
        <process-instance-id>293232</process-instance-id>
        <process-id>ExtensionTest.TestProcess</process-id>
        <process-name>TestProcess</process-name>
        <process-version>1.0</process-version>
        <process-instance-state>1</process-instance-state>
        <container-id>ExtensionTest_1.0.0</container-id>
        <initiator>anton</initiator>
        <start-date>2018-11-05T14:27:07.677+01:00</start-date>
        <process-instance-desc>TestProcess</process-instance-desc>
        <correlation-key>293232</correlation-key>
        <parent-instance-id>-1</parent-instance-id>
        <sla-compliance>0</sla-compliance>
    </process-instance>
</process-instance-list>
```
https://github.com/kiegroup/droolsjbpm-integration/blob/7.11.x/kie-server-parent/kie-server-api/src/main/java/org/kie/server/api/model/instance/ProcessInstanceList.java



## Installation
```
$ cd get-case-instances-with-data-extension
$ mvn clean install
$ cp target/get-case-instances-with-data-extension-1.jar RHPAM_HOME/standalone/deployments/kie-server.war/WEB-INF/lib
```

## Usage
This extension allows you to retrieve all open instances and fetch the desired case data at the same time. This will have some overhead in regards bandwidth spent but eliminates the need of a turnaround between client/server.
The new endpoint is located at:
```
http://localhost:8280/kie-server/services/rest/server/containers/cases/instancesWithData
```

And it's possible to provide query parameter with name 'dataItem'. If you want to provide multiple dataItem (which is possible) then you can either provide a comma separated list:
```
?dataItem=testDataItem,anotherItem
```
or you can chain this parameter multiple times:
```
?dataItem=testDataItem&dataItem=anotherItem
```

- Following usage is demonstrated on IT Orders example which is shipped out of the box with business-central:

- Start the case 
```
 curl -X POST "http://localhost:8280/kie-server/services/rest/server/containers/itorders_1.0.0-SNAPSHOT/cases/itorders.orderhardware/instances" -H "accept: application/xml" -H "content-type: application/json" -d "{ \"case-data\" : { }, \"case-user-assignments\" : { \"owner\" : \"anton\", \"manager\" : \"anton\" }, \"case-group-assignments\" : { \"supplier\" : \"IT\" }}"
 ```
- Store some case data:
```
curl -X POST "http://localhost:8280/kie-server/services/rest/server/containers/itorders_1.0.0-SNAPSHOT/cases/instances/IT-0000000002/caseFile/testDataItem" -H "accept: application/xml" -H "content-type: application/json" -d "\"someValue\""

curl -X POST "http://localhost:8280/kie-server/services/rest/server/containers/itorders_1.0.0-SNAPSHOT/cases/instances/IT-0000000002/caseFile/anotherItem" -H "accept: application/xml" -H "content-type: application/json" -d "\"anotherValue\""

```

- Retrieve all open cases alongside with some data:
```
curl -X GET \
  'http://localhost:8280/kie-server/services/rest/server/containers/cases/instancesWithData?dataItem=testDataItem,anotherItem' \
  -H 'Accept: application/json'
  ```

## Example response
```json
{
    "instances": [
        {
            "case-id": "IT-0000000002",
            "case-description": "Order for IT hardware",
            "case-owner": "anton",
            "case-status": 1,
            "case-definition-id": "itorders.orderhardware",
            "container-id": "itorders_1.0.0-SNAPSHOT",
            "case-started-at": 1541505270248,
            "case-completed-at": null,
            "case-completion-msg": "",
            "case-sla-compliance": 0,
            "case-sla-due-date": null,
            "case-file": {
                "case-data": {
                    "anotherItem": "anotherValue",
                    "testDataItem": "someValue"
                },
                "case-user-assignments": {},
                "case-group-assignments": {},
                "case-data-restrictions": {}
            },
            "case-milestones": null,
            "case-stages": null,
            "case-roles": null
        }
    ]
}
```



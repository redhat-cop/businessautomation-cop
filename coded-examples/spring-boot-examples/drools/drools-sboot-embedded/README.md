KIE Spring Boot Seed App
=============================

An example of a Drools Spring Boot baseline app, demonstrating:

- Multiple KIE Container deployments
- KIE Scanner and reloading of KIE Bases via REST API
- Camel REST and Camel based routing
- KSession Management, Drools Execution via Agenda Groups etc..
- AgendaGroupListener & WorkingMemoryListener
- Swagger UI
- RHOAR managed Spring Boot instance
- Various useful Utils

#### Test

Run the project: 
```
$ mvn spring-boot:run
```

Hit one of the following REST API's : 

```
REQUEST : curl --location --request GET 'http://localhost:8090/api/demo/rules/hello'
RESPONSE { "rulesFired": 1, "message": " Hello World " }

REQUEST curl --location --request GET 'http://localhost:8090/api/demo/rules/goodbye/{name}'
RESPONSE { "rulesFired": 1, "message": " Goodbye Paulo " }
```


#### Useful Links: 

- Context Root: \<HOST>:8090/rest
- KIE Server Base : http://localhost:8090/rest/server
- KIE Server Containers : http://localhost:8090/rest/server/containers
- Swagger JSON: http://localhost:8090/rest/swagger.json
- Swagger UI: http://localhost:8090/rest/api-docs?url=http://localhost:8090/rest/swagger.json
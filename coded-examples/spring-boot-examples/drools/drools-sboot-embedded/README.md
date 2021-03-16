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

- Context Root: \<HOST>:8090/api
- Swagger JSON: http://localhost:8090/api/api-doc
- Swagger UI: http://localhost:8090/webjars/swagger-ui/index.html?url=/api/api-doc
- Spring Management (Actuator): http://localhost:8091/actuator/
- H2 Console: http://localhost:8090/h2-console
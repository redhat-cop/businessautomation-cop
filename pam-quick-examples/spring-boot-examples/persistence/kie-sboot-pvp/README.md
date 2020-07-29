KIE Spring Boot Pluggable Variable Persistence
=============================

An example of a KIE Spring Boot baseline app, demonstrating pluggable variable persistence.

http://mswiderski.blogspot.com/2014/02/jbpm-6-store-your-process-variables.html[Maciej's blog] provides and excellent rundown of the 
how and why. 

This project currently provides 1 example, but aims to add an additional soon.

1. (Implemented) PVP using a Mapped Entity on the same jBPM schema. 
2. (Not Implemeneted) PVP using a Mapped Entity on an alternative schema - https://issues.redhat.com/browse/BAPL-1704

Currently there is an issue with multiple datasources and being defining the PU in which the specified variable gets persisted into. 

#### Run the Example


Run the Seed with a profile: 
```
$ mvn spring-boot:run (Default profile)
```
or 
```
$ mvn spring-boot:run -Dspring.profiles.active=<PROFILE>
```

Useful Links: 
- Context Root: \<HOST>:8090/rest
- KIE Server Base : http://localhost:8090/rest/server
- KIE Server Containers : http://localhost:8090/rest/server/containers
- Swagger JSON: http://localhost:8090/rest/swagger.json
- Swagger UI: http://localhost:8090/rest/api-docs?url=http://localhost:8090/rest/swagger.json

Useful KnowledgeBases:

- http://mswiderski.blogspot.com/2014/02/jbpm-6-store-your-process-variables.html
- https://karinavarela.me/2018/12/22/persisting-custom-process-variables-in-different-db-on-jbpm/
- 
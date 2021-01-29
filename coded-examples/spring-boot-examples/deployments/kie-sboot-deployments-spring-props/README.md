KIE Spring Boot Deployments App
=============================

An example of a KIE Spring Boot baseline app, demonstrating KIE Container deployers via the use of Spring Properties files

Available Spring Profiles: 
- Default (H2 in-memory), 

Run the project with a profile: 
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
KIE Spring Boot Seed App
=============================

An example of a Drools Spring Boot baseline app, demonstrating:

- Multiple KIE Container deployments
- KIE Scanner and reloading of KIE Bases via REST API
- Camel REST and Camel based routing
- KSession Management, Drools Execution via Agenda Groups etc..
- AgendaGroupListener & WorkingMemoryListener
- Rules Audit persisted in DB
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

REQUEST curl -X POST "http://localhost:8090/api/demo/rules/mortgage" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{  \"applicant\": {    \"age\": 17,    \"applicationDate\": \"2021-03-17T15:56:24.149Z\",    \"approved\": false,    \"creditRating\": \"OK\",    \"name\": \"Test\"  },  \"incomeSource\": {    \"amount\": 0,    \"type\": \"string\"  },  \"loanApplication\": {    \"amount\": 0,    \"approved\": true,    \"approvedRate\": 0,    \"deposit\": 0,    \"explanation\": \"string\",    \"insuranceCost\": 0,    \"lengthYears\": 0  }}"

RESPONSE: 
{
  "Applicant": {
    "age": 17,
    "applicationDate": "2021-03-17T15:56:24.149+0000",
    "approved": true,
    "creditRating": "OK",
    "name": "Test"
  },
  "IncomeSource": {
    "amount": 0,
    "type": "string"
  }
}
```

#### Useful Links: 

- Context Root: \<HOST>:8090/api
- Swagger JSON: http://localhost:8090/api/api-doc
- Swagger UI: http://localhost:8090/webjars/swagger-ui/index.html?url=/api/api-doc
- Spring Management (Actuator): http://localhost:8091/actuator/
- H2 Console: http://localhost:8090/h2-console


#### Notes

- The project is based on Java.8
- If you need to execute with a specific version of the spring-boot-maven-plugin the following could prove useful

```
mvn org.springframework.boot:spring-boot-maven-plugin:2.2.2.RELEASE:run
```

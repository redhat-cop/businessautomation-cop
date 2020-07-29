This example shows how to start a process via JMS. This functionality already works in kie-server deployed on JBoss EAP but in spring boot deployment model this requires custom code.

Since this is a PoC, both client and server lives in the same JVM.

To see this example in action simply execute `mvn clean install` in the root of this repository and then `cd spring-boot-kieserver-start-jms && mvn spring-boot:run -Dspring-boot.run.profiles=local`

The client code is stored in `StartProcessJMSClient` - new process is periodically started every 5 seconds.
This client sends a JMS message to embedded broker.
Server code is stored in `CustomKieServerMDB` - it listens for incoming messages and starts an appropriate process as specified in the message sent by client.
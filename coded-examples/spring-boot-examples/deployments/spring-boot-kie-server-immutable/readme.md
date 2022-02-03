## Immutable Spring Boot embedding kie-server

This repository showcase the immutable spring boot configuration. Documentation can be found here:
https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.12/html-single/integrating_red_hat_process_automation_manager_with_other_products_and_components/index#creating-self-contained-image-proc_business-applications

When applied successfully, the KJAR will be packaged within the spring boot fat jar during the _build_ time of the spring boot project. The final archive will be fully self-contained and immutable and upon starting the project, no further calls to maven repository are required.

The configuration is a multi-step procedure:

 - First, in the pom.xml of the spring boot project configure what exact KJAR you want to package with your application:
 ```xml
   <build>
  <plugins>
  <plugin>
    <groupId>org.kie</groupId>
    <artifactId>kie-maven-plugin</artifactId>
    <version>${version.org.kie}</version>
    <executions>
      <execution>
        <id>copy</id>
        <phase>prepare-package</phase>
        <goals>
          <goal>package-dependencies-kjar</goal>
        </goals>
      </execution>
    </executions>
    <configuration>
      <artifactItems>
        <artifactItem>
          <groupId>org.redhat</groupId>
          <artifactId>example-kjar</artifactId>
          <version>1.0-SNAPSHOT</version>
        </artifactItem>
      </artifactItems>
    </configuration>
  </plugin>
  </plugins>
  </build>
  ```
  - Secondly, in `application.properties` specify what KJAR you want to deploy onto your kie-server. This typically means that the configuration in `pom.xml` and in `application.properties` needs to be _in sync_. Example config can look like this:
  ```properties
kieserver.deployments[0].alias=example-kjar
kieserver.deployments[0].containerId=example-kjar
kieserver.deployments[0].artifactId=example-kjar
kieserver.deployments[0].groupId=org.redhat
kieserver.deployments[0].version=1.0.0-SNAPSHOT
kieserver.classPathContainer=true
kieserver.autoScanDeployments=false
```

If you want to avoid specifying `kieserver.deployments` you can set `kieserver.autoScanDeployments` to `true`

- Next step is to `mvn clean install` the `example-kjar`. This kjar needs to be available in some maven repository during the _build time_ of the spring boot project

- Once done, you can `mvn clean package` the `kie-server` project

- OPTIONAL: At this point, you can navigate to `target` folder and `unzip pring-boot-kie-server-immutable-1.0.jar` . You should see the KJAR, as well as its transitive dependencies to be part of the `BOOT-INF/classes/KIE-INF/lib` This is a proof we succesfully applied the configuration

- OPTIONAL: You can also delete the `example-kjar` from a local (or even remote) maven repository. You will see that the spring boot kie-server application will be able to start up anyway

- Final step is to start up the kie-server, by issuing `java -jar target/spring-boot-kie-server-immutable-1.0.jar` command

- If all the configuration has been applied successfully, upon opening `http://localhost:8090/rest/server/containers` in the browser you should see similar response:
```xml
<response type="SUCCESS" msg="List of created containers">
<kie-containers>
<kie-container container-alias="example-kjar" container-id="example-kjar" status="STARTED">
<messages>
<content>Container example-kjar successfully created with module org.redhat:example-kjar:1.0.0-SNAPSHOT.</content>
<severity>INFO</severity>
<timestamp>2022-02-03T17:03:06.757+01:00</timestamp>
</messages>
<release-id>
<artifact-id>example-kjar</artifact-id>
<group-id>org.redhat</group-id>
<version>1.0.0-SNAPSHOT</version>
</release-id>
<scanner status="DISPOSED"/>
</kie-container>
</kie-containers>
</response>
```


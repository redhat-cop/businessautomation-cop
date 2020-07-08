# kie-server-extended-deployment
This repository illustrates a different KJAR deployment approach in spring boot embedding kie-server environment.

By default, if you generate a spring boot kie server application from start.jbpm.org you'll end up with what is called `kie-server state file`, e.g.: `business-application-service.xml`

This file is a main configuration file for deployed KJARs, to deploy one single KJAR, the file can look like this:
```
<kie-server-state>
  <controllers/>
  <configuration>
    <configItems>
      <config-item>
        <name>org.kie.server.location</name>
        <value>http://localhost:8090/rest/server</value>
        <type>java.lang.String</type>
      </config-item>
    </configItems>
  </configuration>
  <containers>
    <container>
      <containerId>business-application-kjar-1_0-SNAPSHOT</containerId>
      <releaseId>
        <groupId>com.company</groupId>
        <artifactId>business-application-kjar</artifactId>
        <version>1.0-SNAPSHOT</version>
      </releaseId>
      <status>STARTED</status>
      <scanner>
        <status>STOPPED</status>
      </scanner>
      <configItems>
        <config-item>
          <name>KBase</name>
          <value></value>
          <type>BPM</type>
        </config-item>
        <config-item>
          <name>KSession</name>
          <value></value>
          <type>BPM</type>
        </config-item>
        <config-item>
          <name>MergeMode</name>
          <value>MERGE_COLLECTIONS</value>
          <type>BPM</type>
        </config-item>
        <config-item>
          <name>RuntimeStrategy</name>
          <value>PER_PROCESS_INSTANCE</value>
          <type>BPM</type>
        </config-item>
      </configItems>
      <messages/>
      <containerAlias>business-application-kjar</containerAlias>
    </container>
  </containers>
</kie-server-state>
```

As you can see, it's quite verbose - 50 lines of XML to maintain for single KJAR. Further, this configuration file is an extra one to what is the central configuration file of your spring boot application (i.e. application.properties or application.yml). Which means you'll have to maintain (at least) two separate files to fully configure this environment.

Good thing about combination of spring boot and kie-server is that it offers a lot of flexibility - and it's possible to get rid of this default state file and move all the configuration to the application.properties (or yml) in rather straightforward way.

First, we'll start with creating a simple POJO which will represent a KJAR to be deployed:

```
public class KJAR {

    private String groupId;
    private String artifactId;
    private String version;
    private String containerId;
    private String alias;
//getters & setters & constructors omitted     
```

Next, we'll modify application.yml and add the list of KJARs to be deployed:
```
deployment:
  kjars:
    - alias: sample-kjar
      artifactId: business-application-kjar
      containerId: business-application-kjar-1_0-SNAPSHOT
      groupId: com.company
      version: 1.0-SNAPSHOT 
 ```
 
The format is quite concise - if you compare it to the default kie-server state file, we have drastically reduced the amount of configuration needed to get the single KJAR deployed.

Finally, we'll create a configuration bean which will iterate over the list of KJARs declared in the yml file and perform the actual deployment:

```
@Configuration
@ConfigurationProperties(prefix = "deployment")
public class KieServerDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerDeployer.class);

    @Autowired
    KieServer kieServer;

    private List<KJAR> kjars;

    @Bean
    CommandLineRunner deployAndValidate() {
        return new CommandLineRunner() {

            @Override
            public void run(String... strings) throws Exception {
            
                LOGGER.info("Kie Containers listed for deployments :: {}", kjars);

                // Check which containers are already deployed
                List<KieContainerResource> result = kieServer.listContainers(KieContainerResourceFilter.ACCEPT_ALL).getResult().getContainers();
                result.forEach(c -> {
                    LOGGER.info("KIE Containers already deployed {}", c);
                });

                // Deploy containers specified in props
                kjars.forEach(k -> {
                    KieContainerResource resource = new KieContainerResource(k.getContainerId(), k.getReleaseId());
                    resource.setResolvedReleaseId(k.getReleaseId());
                    resource.setContainerAlias(k.getAlias());

                    if (!isDeployed(resource, result)) {
                        LOGGER.info("Deploying KIE Container : {} using custom deployer", k);
                        resource.setResolvedReleaseId(null);
                        kieServer.createContainer(k.getContainerId(), resource);
                    } else {
                        LOGGER.info("Skipping deployment of KIE Container : {} b/c it's already deployed", k);
                    }
                });
            }
        };
    }
```

and that's it! There is one thing to notice here - the kie-server state file will be created by kie-server anyway. This will be done *after* the code above was executed. We do not necesarily need to care about this in the cloud environment - because on our container restart, this file will be removed (due to ephemeral nature). However, in local environment, this file will likely stay in your current working directory between application restart and that's why the *isDeployed* method is implemented in the bean above - because if that state file is indeed found by kie-server, it's processed *before* the code above. Another approach how to mitigate this would be to create a bean with shutdown hook which would delete the kie-server state file just before the JVM shutdown.

To test, execute `mvn clean install` inside model and kjar project. Then execute `mvn spring-boot:run` in service project.
 
 



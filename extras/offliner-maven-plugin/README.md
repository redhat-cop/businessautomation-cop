![Java CI with Maven for offliner-maven-plugin](https://github.com/redhat-cop/businessautomation-cop/workflows/Java%20CI%20with%20Maven%20for%20offliner-maven-plugin/badge.svg)

# offliner-maven-plugin

usage example
```
<plugin>
   <groupId>com.redhat.cop.pam</groupId>
   <artifactId>offliner-maven-plugin</artifactId>
   <version>1.0-SNAPSHOT</version>
   <executions>
      <execution>
         <phase>prepare-package</phase>
         <goals>
            <goal>offliner</goal>
         </goals>
         <configuration>
            <settingsFile>src/test/resources/com/redhat/cop/pam/offlinermavenplugin/settings.xml</settingsFile>
            <outputDirectory>target/test-classes/repository/</outputDirectory>
            <artifacts>
               <gav>org.jboss.javaee:jboss-transaction-api:1.0.1.GA</gav>
               <gav>javax.xml.bind:jaxb-api:2.3.1</gav>
            </artifacts>
         </configuration>
      </execution>
   </executions>
</plugin>
```

|property name|property description|mandatory|default value|property type|property example|
|-------------|--------------------|---------|-------------|-------------|----------------|
|settingsFile|Path to user settings.xml file|No|/home/${user}/.m2/settings.xml|String|src/test/resources/com/redhat/cop/pam/offlinermavenplugin/settings.xml|
|outputDirectory|Path where the repository have to be created|No|${project.home.dir}/target/classes/repository/|String|target/test-classes/repository/|
artifacts|List of GAV to include in the repository|Yes||List<String>|<pre>`<artifacts>`<br/>`<gav>org.jboss.javaee:jboss-transaction-api:1.0.1.GA</gav>`<br/>`<gav>javax.xml.bind:jaxb-api:2.3.1</gav>`<br/>`</artifacts>`</pre>|









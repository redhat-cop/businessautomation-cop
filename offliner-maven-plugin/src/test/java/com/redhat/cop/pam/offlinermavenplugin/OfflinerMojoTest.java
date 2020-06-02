package com.redhat.cop.pam.offlinermavenplugin;

import java.io.File;
import java.nio.file.Paths;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Test;

public class OfflinerMojoTest extends AbstractMojoTestCase {

    /**
     * Test the plugin working fine with
     * - more then one GAV declared in plugin configuration
     * - transitive dependencies
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        final File testPom = new File(getBasedir(), "src/test/resources/com/redhat/cop/pam/offlinermavenplugin/pom.xml");
        final OfflinerMojo mojo = (OfflinerMojo) lookupMojo("offliner", testPom);
        assertNotNull(mojo);
        mojo.execute();
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar").toFile().exists());
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.pom").toFile().exists());
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar").toFile().exists());
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.pom").toFile().exists());
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/org/jboss/javaee/jboss-transaction-api/1.0.1.GA/jboss-transaction-api-1.0.1.GA.jar").toFile().exists());
        assertTrue(Paths.get( getBasedir(), "target/test-classes/", "repository/org/jboss/javaee/jboss-transaction-api/1.0.1.GA/jboss-transaction-api-1.0.1.GA.pom").toFile().exists());
    }

}
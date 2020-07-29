package org.redhat.services.test.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.redhat.services.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class KieServerClientServiceTest {

    @Autowired
    Environment environment;

    @Value("${kie.service.account.username:kris}")
    private String serviceAccountUser;

    @Value("${kie.service.account.password:password1!}")
    private String serviceAccountPwd;

    @Value("${kie.url:localhost}")
    private String kieUrl;

    @Value("${cxf.path}")
    private String kieContextPath;

    private static final String KIE_PATH = "/server";

    @Bean(name = "kieServerClient")
    public KieServicesClient clientProducer() throws MalformedURLException, URISyntaxException {
        KieServicesConfiguration configuration = KieServicesFactory
                .newRestConfiguration(this.buildURL(), serviceAccountUser, serviceAccountPwd);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);

        List<String> capabilities = new ArrayList<>();
        capabilities.add(KieServerConstants.CAPABILITY_BPM);
        capabilities.add(KieServerConstants.CAPABILITY_BRM);
        capabilities.add(KieServerConstants.CAPABILITY_BPM_UI);
        capabilities.add(KieServerConstants.CAPABILITY_CASE);
        capabilities.add(KieServerConstants.CAPABILITY_DMN);
        capabilities.add(KieServerConstants.CAPABILITY_SWAGGER);
        configuration.setCapabilities(capabilities);

        Set<Class<?>> classes = new HashSet<>();
        classes.add(Person.class);
        configuration.addExtraClasses(classes);

        return KieServicesFactory.newKieServicesClient(configuration);
    }

    /**
     * Build URL Path to KIE Server e.g. http://localhost:8090/rest/server
     *
     * @return
     */
    public String buildURL() throws URISyntaxException, MalformedURLException {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http");
        builder.setHost(kieUrl);
        builder.setPort(8090);
        builder.setPath(kieContextPath.trim() + "/server");
        URL url = builder.build().toURL();

        log.info("KIE URL :: {}", url.toString());
        return url.toString();
    }
}

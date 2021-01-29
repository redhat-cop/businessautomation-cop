package org.redhat.services.security;

import org.jbpm.springboot.autoconfigure.JBPMAutoConfiguration;
import org.jbpm.springboot.autoconfigure.JBPMProperties;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SpringSecurityConfiguration extends JBPMAutoConfiguration {

    public SpringSecurityConfiguration(PlatformTransactionManager transactionManager, JBPMProperties properties, ApplicationContext applicationContext) {
        super(transactionManager, properties, applicationContext);
    }


    @Bean
    @ConditionalOnMissingBean(
            name = {"identityProvider"}
    )
    public IdentityProvider identityProvider() {
        return new SpringSecurityIdentityProvider();
    }

    @Bean
    @ConditionalOnMissingBean(
            name = {"userGroupCallback"}
    )
    public UserGroupCallback userGroupCallback(IdentityProvider identityProvider) {
        return new SpringSecurityUserGroupCallback(identityProvider);
    }

}

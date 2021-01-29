package com.company.history.jms.server;

import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "local", "local-case" })
public class EmbeddedBrokerConfig {

	private Logger logger = LoggerFactory.getLogger(EmbeddedBrokerConfig.class);

	@Bean
	@Profile({ "local", "local-case" })
	public ActiveMQJAASSecurityManager securityManager(@Value("${amq.broker.user}") String user,
			@Value("${amq.broker.password}") String password, @Value("${amq.broker.role}") String role) {
		final SecurityConfiguration configuration = new SecurityConfiguration();
		final ActiveMQJAASSecurityManager securityManager = new ActiveMQJAASSecurityManager(
				InVMLoginModule.class.getName(), configuration);
		configuration.addUser(user, password);
		configuration.addRole(user, role);
		configuration.setDefaultUser(user);

		return securityManager;
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	@Profile({ "local", "local-case" })
	public EmbeddedActiveMQ embeddedActiveMQ(ActiveMQJAASSecurityManager securityManager) {
		logger.info("Initializaint Embedded AMQ broker");
		final EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
		embeddedActiveMQ.setSecurityManager(securityManager);
		return embeddedActiveMQ;
	}

}

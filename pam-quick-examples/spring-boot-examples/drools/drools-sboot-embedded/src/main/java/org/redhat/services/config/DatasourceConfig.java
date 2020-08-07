package org.redhat.services.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// @formatter:off
@Slf4j
@EnableJpaRepositories(
		entityManagerFactoryRef = "entityManager",
		transactionManagerRef = "transactionManager",
		basePackages = { "org.redhat.services.repository" })
// @formatter:on
@Configuration
@EnableTransactionManagement
public class DatasourceConfig {

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "datasource")
    @ConfigurationProperties("drools.datasource")
    public DataSource dataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        return ds;
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager psqlTransactionManager(@Qualifier("entityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);

    }

    @Primary
    @Bean(name = "entityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) throws SQLException {
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", env.getProperty("drools.hbm2ddl"));
        props.put("hibernate.dialect", env.getProperty("drools.dialect"));
        //props.put("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");

        DataSource ds = dataSource();
        ds.getConnection(); // eager connection pool init

        return builder.dataSource(ds) //
                .packages("org.redhat.services.model.entity") //
                .persistenceUnit("drools-pu") //
                .properties(props) //
                .build(); //
    }

}

package org.redhat.services.configuration;

import lombok.extern.slf4j.Slf4j;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.jbpm.springboot.autoconfigure.JBPMAutoConfiguration;
import org.jbpm.springboot.autoconfigure.JBPMProperties;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Overrides existing jBPM Datasource simply for @Primary on the datasource..
 * This shall be fixed soon..
 */
@Slf4j
@Configuration
@Profile("multi-db")
public class JBPMPersistence extends JBPMAutoConfiguration {

    private ApplicationContext applicationContext;

    private PlatformTransactionManager transactionManager;

    public JBPMPersistence(PlatformTransactionManager transactionManager, JBPMProperties properties, ApplicationContext applicationContext) {
        super(transactionManager, properties, applicationContext);
        this.applicationContext = applicationContext;
    }

    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    protected static final String PERSISTENCE_UNIT_NAME = "org.jbpm.domain";
    protected static final String PERSISTENCE_XML_LOCATION = "classpath:/META-INF/jbpm-persistence.xml";

    @Autowired
    @Qualifier("auditEntityManager")
    EntityManagerFactory auditEMF;

    /**
     * Define jBPM EMF as Primary
     * Overriding Class: https://github.com/kiegroup/droolsjbpm-integration/blob/master/kie-spring-boot/kie-spring-boot-autoconfiguration/jbpm-spring-boot-autoconfiguration/src/main/java/org/jbpm/springboot/autoconfigure/JBPMAutoConfiguration.java#L194
     *
     * @param dataSource
     * @param jpaProperties
     * @return
     */
    @Override
    @Bean
    @Primary // Allows to specify additional DS's
    @ConditionalOnMissingBean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, JpaProperties jpaProperties) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
        factoryBean.setPersistenceXmlLocation(PERSISTENCE_XML_LOCATION);
        factoryBean.setJtaDataSource(dataSource);
        factoryBean.setJpaPropertyMap(jpaProperties.getProperties());
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setPrepareConnection(false);
        factoryBean.setJpaVendorAdapter(adapter);

        String packagesToScan = jpaProperties.getProperties().get("entity-scan-packages");
        if (packagesToScan != null) {
            factoryBean.setPersistenceUnitPostProcessors(new PersistenceUnitPostProcessor() {

                @Override
                public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
                    Set<TypeFilter> entityTypeFilters = new LinkedHashSet<TypeFilter>(3);
                    entityTypeFilters.add(new AnnotationTypeFilter(Entity.class, false));
                    entityTypeFilters.add(new AnnotationTypeFilter(Embeddable.class, false));
                    entityTypeFilters.add(new AnnotationTypeFilter(MappedSuperclass.class, false));

                    ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

                    if (packagesToScan != null) {
                        for (String pkg : packagesToScan.split(",")) {
                            try {
                                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                                        ClassUtils.convertClassNameToResourcePath(pkg) + CLASS_RESOURCE_PATTERN;
                                Resource[] resources = resourcePatternResolver.getResources(pattern);
                                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                                for (Resource resource : resources) {
                                    if (resource.isReadable()) {
                                        MetadataReader reader = readerFactory.getMetadataReader(resource);
                                        String className = reader.getClassMetadata().getClassName();
                                        if (matchesFilter(reader, readerFactory, entityTypeFilters)) {
                                            pui.addManagedClassName(className);
                                        } else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                                            pui.addManagedPackage(className.substring(0, className.length() - PACKAGE_INFO_SUFFIX.length()));
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                throw new PersistenceException("Failed to scan classpath for unlisted entity classes", ex);
                            }
                        }
                    }

                }

                private boolean matchesFilter(MetadataReader reader, MetadataReaderFactory readerFactory, Set<TypeFilter> entityTypeFilters) throws IOException {
                    for (TypeFilter filter : entityTypeFilters) {
                        if (filter.match(reader, readerFactory)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        return factoryBean;
    }
}

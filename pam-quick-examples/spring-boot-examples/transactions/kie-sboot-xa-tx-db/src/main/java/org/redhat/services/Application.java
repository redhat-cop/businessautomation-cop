package org.redhat.services;

import org.redhat.services.jpa.TestEntity;
import org.redhat.services.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    @Qualifier("auditEntityManager")
    EntityManagerFactory auditEMF;

    @Autowired
    TestRepository repository;

    @Autowired
    @Qualifier("auditJDBCTemplate")
    JdbcTemplate template;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Application Started !!");

        final String sql = "SELECT * FROM TEST_ENTITY";

        TestEntity entity = new TestEntity();
        entity.setDescription("Hello World");
        entity.setProcessInstanceId(1L);
        repository.save(entity);

        //  Spring Repo
        repository.findAll().forEach((ent) -> {
            log.info("Repository: {}", ent);
        });

        // Native EMF
        EntityManager em = auditEMF.createEntityManager();
        List<TestEntity> emEnts = em.createNativeQuery(sql, TestEntity.class).getResultList();
        emEnts.forEach((ent) -> {
            log.info("EntityManager: {}", ent);
        });

        // JDBCTemplate
        List<TestEntity> templateEnts = template.query(
                sql,
                new BeanPropertyRowMapper(TestEntity.class));

        templateEnts.forEach((ent) -> {
            log.info("JDBCTemplate: {}", ent);
        });
    }

}
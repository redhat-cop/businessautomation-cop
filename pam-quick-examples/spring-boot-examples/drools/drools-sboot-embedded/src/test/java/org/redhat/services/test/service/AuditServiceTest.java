package org.redhat.services.test.service;//package org.redhat.services.test.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Test;
import org.redhat.services.model.entity.ExecutionLog;
import org.redhat.services.model.type.ExecutionStatus;
import org.redhat.services.service.api.AuditService;
import org.redhat.services.test.AppTestBase;
import org.redhat.services.test.RepoTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test AuditService
 */
@Slf4j
public class AuditServiceTest extends AppTestBase {

    @Autowired
    private AuditService auditService;

    @Test
    public void auditService() throws Exception {

        String ref = UUID.randomUUID().toString();

        ExecutionLog executionLog = new ExecutionLog();
        executionLog.setExecutionReference(ref);
        executionLog.setRulesExecutionDuration(100);
        executionLog.setTotalRulesFired(1);
        executionLog.setExecutionStop(LocalDateTime.now());
        executionLog.setExecutionStart(executionLog.getExecutionStop().minusNanos(executionLog.getRulesExecutionDuration()));
        executionLog.setAgendaGroup("test");
        executionLog.setRulesFired("test");
        executionLog.setStatus(ExecutionStatus.SUCCESS);
        executionLog.setPayload("Hello World");

        auditService.persist(executionLog);

        log.info("Running RxSales REPO test");

        // Obtain Log
        ExecutionLog log = auditService.findByReference(ref);

        // Test Obtained obj
        assertThat(log).isNotNull();
        assertThat(log.getId()).isGreaterThan(0);
        assertThat(log.getExecutionReference()).isEqualTo(ref);
        assertThat(log.getPayload()).isEqualTo(executionLog.getPayload());
        assertThat(log.getTotalRulesFired()).isEqualTo(executionLog.getTotalRulesFired());

    }
    
}

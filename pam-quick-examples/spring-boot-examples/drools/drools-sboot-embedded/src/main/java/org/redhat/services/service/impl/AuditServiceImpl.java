package org.redhat.services.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.redhat.services.model.entity.ExecutionLog;
import org.redhat.services.repository.ExecutionLogRepository;
import org.redhat.services.service.api.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    ExecutionLogRepository executionLogRepository;

    @Override
    public void persist(ExecutionLog executionLog) {
        log.info("Persisting ExecutionLog {}", executionLog);
        executionLogRepository.saveAndFlush(executionLog);
    }

    @Override
    public ExecutionLog findByReference(String reference) {
        log.info("Obtaining ExecutionLog by Reference : {}", reference);
        return executionLogRepository.findByExecutionReference(reference);
    }
}

package org.redhat.services.service.api;//

import org.redhat.services.model.entity.ExecutionLog;

import java.util.List;

public interface AuditService {

    void persist(ExecutionLog executionLog);

    ExecutionLog findByReference(String reference);

}

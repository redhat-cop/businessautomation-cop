package org.redhat.services.service.api;//

import org.redhat.services.model.entity.ExecutionLog;

public interface AuditService {

    void persist(ExecutionLog executionLog);

    ExecutionLog findByReference(String reference);

}

package org.redhat.services.repository;

import org.redhat.services.model.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {

    ExecutionLog findByExecutionReference(String reference);
}


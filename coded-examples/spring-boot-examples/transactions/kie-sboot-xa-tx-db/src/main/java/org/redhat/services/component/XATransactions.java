package org.redhat.services.component;

import org.redhat.services.jpa.TestEntity;
import org.redhat.services.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.jbpm.services.api.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class XATransactions {

    public static final String containerId = "kie-sboot-test-kjar-1_0-SNAPSHOT";
    public static final String processDefId = "SampleProcess";

    @Autowired
    private ProcessService processService;

    @Autowired
    TestRepository repository;

    /**
     * 1. Create jBPM instance
     * 2. Persist to local database
     * 3. Throw exception and rollback above
     * @param throwException
     * @param params
     * @throws Exception
     */
    @Transactional(rollbackFor=Exception.class, propagation = Propagation.REQUIRED)
    public void transactionInitiatedBeforeOutsideJBPM(boolean throwException, Map<String, Object> params) throws Exception {

        // 1. Create jBPM instance
        long processInstanceId = processService.startProcess(containerId, processDefId, params);

        TestEntity entity = new TestEntity();
        entity.setDescription("Hello World :: " + processInstanceId);
        entity.setProcessInstanceId(processInstanceId);

        // 2. Persist to local database
        log.info(" Saving Test Entity in different context :D outside of BPM ");
        repository.save(entity);

        // 3. Throw exception and rollback above
        if (throwException) {
            throw new Exception("Something went wrong again bro... D:");
        }
    }

    /**
     * 1. Persist to local database
     * 2. Create jBPM instance
     * 3. Throw exception and rollback above
     * @param throwException
     * @param params
     * @throws Exception
     */
    @Transactional(rollbackFor=Exception.class, propagation = Propagation.REQUIRED)
    public void transactionInitiatedOutsideAfterJBPM(boolean throwException, Map<String, Object> params) throws Exception {

        long random = new Random().nextLong();
        TestEntity entity = new TestEntity();
        entity.setDescription("Hello World :: " + random);
        entity.setProcessInstanceId(random);

        // 1. Persist to local database
        log.info(" Saving Test Entity in different context :D outside of BPM ");
        repository.save(entity);

        //  2. Create jBPM instance
        long processInstanceId = processService.startProcess(containerId, processDefId, params);

        // 3. Throw exception and rollback above
        if (throwException) {
            throw new Exception("Something went wrong again bro... D:");
        }
    }
}

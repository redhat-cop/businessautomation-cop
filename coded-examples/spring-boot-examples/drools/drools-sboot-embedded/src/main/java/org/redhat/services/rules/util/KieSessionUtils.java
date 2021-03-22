package org.redhat.services.rules.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.redhat.services.kie.listener.RuleFiredEventListener;
import org.redhat.services.kie.listener.WorkingMemoryListener;
import org.redhat.services.model.entity.ExecutionLog;
import org.redhat.services.model.type.ExecutionStatus;
import org.redhat.services.util.GeneralUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.redhat.services.util.RoutingConstants.AUDIT;

/**
 * KIE Session Utils to handle KIE API & Rules Execution
 *
 * @author ajarrett
 */
@Slf4j
@Component
public class KieSessionUtils {

    @Value("${drools.persist.payload:false}")
    boolean persistPayload;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GeneralUtils utils;

    @Autowired
    private ProducerTemplate producerTemplate;

    private static final String rulesBasePackage = "org.redhat.services.rules.";

    private int counter;
    private Long numberOfFacts;

    private static final String kieBaseName = "defaultKieBase";

    public Function<KieContainer, KieSession> createNewKieSession = (container) -> {
        final long executionStart = System.currentTimeMillis();
        final KieSession kieSession = container.getKieBase(kieBaseName).newKieSession();
        log.debug("{} kie session created in {}ms", kieBaseName, (System.currentTimeMillis() - executionStart));
        return kieSession;
    };

    public Function<KieSession, KieSession> addSessionListeners = (kieSession) -> {
        kieSession.addEventListener(new RuleFiredEventListener());
        return kieSession;
    };

    public BiFunction<KieSession, Object, KieSession> insertFact = (kieSession, fact) -> {
        kieSession.insert(fact);
        return kieSession;
    };

    public BiFunction<KieSession, Object[], KieSession> insertFacts = (kieSession, facts) -> {

        counter = 0;
        numberOfFacts = 0L;
        Stream.of(facts).filter(fact -> fact != null).forEach(fact -> {

            insertFact.apply(kieSession, fact);
            counter++;
            numberOfFacts++;

            // TODO: Refactor this into something better or default to listeners
            if (counter >= 1000) {
                counter = 0;
                log.debug("Inserted {} objects of type {} into KIE Session with id: {}", numberOfFacts, fact.getClass(),
                        kieSession.getIdentifier());
            }
        });
        log.debug("Inserted a TOTAL of {} objects into KIE Session with id: {}", numberOfFacts, kieSession.getIdentifier());
        return kieSession;
    };

    public BiFunction<KieSession, Entry<String, Object>, KieSession> setKieSessionGlobal = (kieSession, globalVariableEntry) -> {
        kieSession.setGlobal(globalVariableEntry.getKey(), globalVariableEntry.getValue());
        return kieSession;
    };

    public BiFunction<KieSession, Map<String, Object>, KieSession> setKieSessionGlobals = (kieSession, globalVariablesMap) -> {
        globalVariablesMap.entrySet().stream().forEach(entry -> setKieSessionGlobal.apply(kieSession, entry));
        return kieSession;
    };

    public BiFunction<KieSession, String, KieSession> setAgendaGroup = (kieSession, ruleFlowGroupName) -> {
        kieSession.getAgenda().getAgendaGroup(ruleFlowGroupName).setFocus();
        return kieSession;
    };

    public BiFunction<KieSession, Integer, KieSession> fireAllRulesWithLimit = (kieSession, limit) -> {
        int i = kieSession.fireAllRules(limit);
        log.debug("Throttling rule fired:" + i);
        return kieSession;
    };

    public BiFunction<KieSession, String, KieSession> fireAgendaGroupRules = (kieSession, agendaGroupName) -> {

        String reference = getKieSessionExecutionReference(kieSession);

        log.info("Using execution reference: {}", reference);

        // Add Rules Debuggers/Loggers
        if (log.isDebugEnabled() || log.isTraceEnabled()) {
            kieSession.addEventListener(new RuleFiredEventListener(reference));
            kieSession.addEventListener(new WorkingMemoryListener(reference));
        }

        final long executionStart = System.currentTimeMillis();
        ExecutionLog executionLog = buildExecutionLog(reference, executionStart, kieSession, agendaGroupName);

        log.info("=================================== START DROOLS EXECUTION AGENDA GROUP : {} ===================================",
                agendaGroupName.toUpperCase());

        kieSession.getAgenda().getAgendaGroup(agendaGroupName).setFocus();
        final int rulesFired = kieSession.fireAllRules();

        executionLog = this.updateExecutionLog(executionLog, System.currentTimeMillis(), executionStart, rulesFired);

        log.info("{} :: end execution of {} agendaGroup, {} rules has been fired in {} ms", reference, agendaGroupName, rulesFired,
                executionLog.getRulesExecutionDuration());
        log.info("==================================== END DROOLS EXECUTION AGENDA GROUP : {} ====================================",
                agendaGroupName.toUpperCase());

        // audit execution
        this.persistExecutionLog(executionLog);

        return kieSession;
    };

    public BiFunction<KieSession, String, KieSession> fireAgendaFilterPackageRules = (kieSession, agendaFilterName) -> {

        String reference = getKieSessionExecutionReference(kieSession);

        log.info("Using execution reference: {}", reference);

        final long executionStart = System.currentTimeMillis();
        ExecutionLog executionLog = buildExecutionLog(reference, executionStart, kieSession, agendaFilterName);
        final String packageName = KieSessionUtils.rulesBasePackage + agendaFilterName;

        if (log.isDebugEnabled() || log.isTraceEnabled()) {
            kieSession.addEventListener(new RuleFiredEventListener(reference));
            kieSession.addEventListener(new WorkingMemoryListener(reference));
        }

        log.info("=================================== START DROOLS EXECUTION AGENDA FILTER : {} ===================================",
                packageName.toUpperCase());

        final int rulesFired = kieSession.fireAllRules(new AgendaFilter() {
            @Override
            public boolean accept(Match match) {
                String rulePackage = match.getRule().getPackageName();

                if (rulePackage.equalsIgnoreCase(packageName)) {
                    return true;
                }
                return false;
            }
        });
        executionLog = this.updateExecutionLog(executionLog, System.currentTimeMillis(), executionStart, rulesFired);

        log.info("end execution of {} agendaFilter, {} rules has been fired in {} ms", packageName, rulesFired,
                (System.currentTimeMillis() - executionStart));
        log.info("==================================== END DROOLS EXECUTION AGENDA GROUP : {} ====================================",
                packageName.toUpperCase());

        // async audit execution
        this.persistExecutionLog(executionLog);

        return kieSession;
    };

    public Function<KieSession, KieSession> fireAllRules = (kieSession) -> {

        String reference = getKieSessionExecutionReference(kieSession);

        log.info("Using execution reference: {}", reference);

        if (log.isDebugEnabled() || log.isTraceEnabled()) {
            kieSession.addEventListener(new RuleFiredEventListener(reference));
            kieSession.addEventListener(new WorkingMemoryListener(reference));
        }

        final long executionStart = System.currentTimeMillis();
        ExecutionLog executionLog = buildExecutionLog(reference, executionStart, kieSession, null);

        log.info("=================================== START {} ===================================", "DROOLS EXECUTION RULES");
        final int rulesFired = kieSession.fireAllRules();
        executionLog = this.updateExecutionLog(executionLog, System.currentTimeMillis(), executionStart, rulesFired);

        log.info("end execution of {} rules, {} rules has been fired in {} ms", "Pre verification check", rulesFired,
                (System.currentTimeMillis() - executionStart));
        log.info("==================================== END {} ====================================", "DROOLS EXECUTION RULES");

        // async audit execution
        this.persistExecutionLog(executionLog);

        return kieSession;
    };

    public Function<KieSession, Void> tearDown = (kieSession) -> {
        log.info("Dispose of KIE Session with id {}", kieSession.getIdentifier());
        kieSession.dispose();
        kieSession.destroy();
        return null;
    };

    private ExecutionLog buildExecutionLog(String reference, Long start, KieSession session, String agendaGroup) {
        ExecutionLog executionLog = new ExecutionLog();
        executionLog.setExecutionReference(reference);
        executionLog.setExecutionStart(millsToLocalDateTime(start));

        // persist json repo of working memory to jdbc
        if (persistPayload) {
            Map<String, Object> payload = new HashMap<>();
            Iterator<FactHandle> it = session.getFactHandles().iterator();
            while (it.hasNext()) {
                FactHandle handle = it.next();
                Object obj = session.getObject(handle);
                payload.put(handle.toExternalForm(), obj);
            }
            executionLog.setPayload(utils.toJson(payload));
        }

        executionLog.setStatus(ExecutionStatus.IN_PROGRESS);
        executionLog.setAgendaGroup(agendaGroup);
        return executionLog;
    }

    private ExecutionLog updateExecutionLog(ExecutionLog executionLog, long end, long start, int rulesFired) {
        executionLog.setExecutionStop(millsToLocalDateTime(end));
        executionLog.setRulesExecutionDuration(end - start);
        executionLog.setTotalRulesFired(rulesFired);
        executionLog.setStatus(ExecutionStatus.SUCCESS);
        return executionLog;
    }

    private void persistExecutionLog(ExecutionLog executionLog) {
        log.info("persisting executionLog {} asynchronously to endpoint {}", executionLog, AUDIT.EXECUTION_LOG);
        producerTemplate.asyncSendBody(AUDIT.EXECUTION_LOG, executionLog);
    }

    public static LocalDateTime millsToLocalDateTime(long millis) {
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime date = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return date;
    }

    public String getKieSessionExecutionReference(KieSession kieSession) {
        return (String) kieSession.getGlobal("reference");
    }

}

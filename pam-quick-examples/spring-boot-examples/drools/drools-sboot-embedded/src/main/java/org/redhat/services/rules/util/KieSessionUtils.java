package org.redhat.services.rules.util;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;
import org.redhat.services.kie.listener.RuleFiredEventListener;
import org.redhat.services.kie.listener.WorkingMemoryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * KIE Session Utils to handle KIE API & Rules Execution
 * 
 * @author ajarrett
 */
@Component
public class KieSessionUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(KieSessionUtils.class);

	private static final String rulesBasePackage = "org.redhat.services.rules.";

	private int counter;
	private Long numberOfFacts;

	private static final String kieBaseName = "defaultKieBase";

	public Function<KieContainer, KieSession> createNewKieSession = (container) -> {
		final long executionStart = System.currentTimeMillis();
		final KieSession kieSession = container.getKieBase(kieBaseName).newKieSession();
		LOGGER.debug("{} kie session created in {}ms", kieBaseName, (System.currentTimeMillis() - executionStart));
		return kieSession;
	};
	
	public Function<KieContainer, KieSession> createNewPingSession = (container) -> {
		final long executionStart = System.currentTimeMillis();
		final KieSession kieSession = container.newKieSession();
		LOGGER.debug("{} kie session created in {}ms", kieBaseName, (System.currentTimeMillis() - executionStart));
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
			
			// TODO: Review this into something better..
			if (counter >= 1000) {
				counter = 0;
				LOGGER.debug("Inserted {} objects of type {} into KIE Session with id: {}", numberOfFacts, fact.getClass(),
						kieSession.getIdentifier());
			}
		});
		LOGGER.debug("Inserted a TOTAL of {} objects into KIE Session with id: {}", numberOfFacts, kieSession.getIdentifier());
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

		LOGGER.debug("Throttling rule fired:" + i);

		return kieSession;
	};

	public BiFunction<KieSession, String, KieSession> fireAgendaGroupRules = (kieSession, agendaGroupName) -> {

		// Add Rules Debuggers/Loggers
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			kieSession.addEventListener(new RuleFiredEventListener());
			kieSession.addEventListener(new WorkingMemoryListener());
		} 
		
		final long executionStart = System.currentTimeMillis();
		LOGGER.info("=================================== START DROOLS EXECUTION AGENDA GROUP : {} ===================================",
				agendaGroupName.toUpperCase());
		kieSession.getAgenda().getAgendaGroup(agendaGroupName).setFocus();
		final int rulesFired = kieSession.fireAllRules();
		LOGGER.info("end execution of {} agendaGroup, {} rules has been fired in {} ms", agendaGroupName, rulesFired,
				(System.currentTimeMillis() - executionStart));
		LOGGER.info("==================================== END DROOLS EXECUTION AGENDA GROUP : {} ====================================",
				agendaGroupName.toUpperCase());
		return kieSession;
	};

	public BiFunction<KieSession, String, KieSession> fireAgendaFilterPackageRules = (kieSession, agendaFilterName) -> {
		final long executionStart = System.currentTimeMillis();
		final String packageName = KieSessionUtils.rulesBasePackage + agendaFilterName;
		
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			kieSession.addEventListener(new RuleFiredEventListener());
			kieSession.addEventListener(new WorkingMemoryListener());
		} 

		LOGGER.info("=================================== START DROOLS EXECUTION AGENDA FILTER : {} ===================================",
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

		LOGGER.info("end execution of {} agendaFilter, {} rules has been fired in {} ms", packageName, rulesFired,
				(System.currentTimeMillis() - executionStart));
		LOGGER.info("==================================== END DROOLS EXECUTION AGENDA GROUP : {} ====================================",
				packageName.toUpperCase());
		return kieSession;
	};

	public Function<KieSession, KieSession> fireAllRules = (kieSession) -> {
		
		if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
			kieSession.addEventListener(new RuleFiredEventListener());
			kieSession.addEventListener(new WorkingMemoryListener());
		} 
		
		final long executionStart = System.currentTimeMillis();
		LOGGER.info("=================================== START {} ===================================", "DROOLS EXECUTION RULES");
		final int rulesFired = kieSession.fireAllRules();

		LOGGER.info("end execution of {} rules, {} rules has been fired in {} ms", "Pre verification check", rulesFired,
				(System.currentTimeMillis() - executionStart));
		LOGGER.info("==================================== END {} ====================================", "DROOLS EXECUTION RULES");
		return kieSession;
	};

	public Function<KieSession, Void> tearDown = (kieSession) -> {
		LOGGER.info("Dispose of KIE Session with id {}", kieSession.getIdentifier());
		kieSession.dispose();
		kieSession.destroy();
		return null;
	};

}

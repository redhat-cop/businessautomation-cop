package org.redhat.services.rules.util;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.redhat.services.model.RuleResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@Slf4j
@Component
public class KieQueryUtils {

    @SuppressWarnings("unchecked")
    public BiFunction<KieSession, AtomicReference<List<RuleResponse>>, KieSession> getRuleResponse = (kieSession, ruleResponses) -> {

        log.info("Obtaining RuleResponse results from KieSession");
        Collection<Object> result = (Collection<Object>) kieSession.getObjects(obj -> obj instanceof RuleResponse);

        log.info("Collection Rule Result : {}", result);

        if (!result.isEmpty()) {

            List<RuleResponse> resultList = new ArrayList<RuleResponse>();
            result.stream().forEach(o -> resultList.add((RuleResponse) o));

            log.debug("{} RuleResponse results obtained from KieSession", resultList.size());
            ruleResponses.set(resultList);
        }

        return kieSession;
    };

    @SuppressWarnings("unchecked")
    public BiFunction<KieSession, AtomicReference<List<Object>>, KieSession> getAllFacts = (kieSession, facts) -> {

        log.info("Obtaining all facts from KieSession");
        Collection<Object> result = (Collection<Object>) kieSession.getObjects();

        log.info("Facts Collection : {}", result);

        if (!result.isEmpty()) {
            log.debug("{} Facts obtained from KieSession", result.size());
            facts.set(Arrays.asList(result.toArray()));
        }

        return kieSession;
    };

}

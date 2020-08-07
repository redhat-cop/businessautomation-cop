package org.redhat.services.rules.util;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.redhat.services.model.RuleResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

@Slf4j
@Component
public class KieQueryUtils {

//	public BiFunction<KieSession, AtomicReference<SuggestionTypes>, KieSession> getSuggestionTypes = (kieSession, suggestionTypes) -> {
//		QueryResults results = kieSession.getQueryResults("getTriggerSuggestionTypes");
//		if (results != null && results.size() > 0) {
//			QueryResultsRow row = Iterables.get(results, 0);
//			if (row.get("$SuggestionTypes") != null) {
//				suggestionTypes.set((SuggestionTypes) row.get("$SuggestionTypes"));
//			}
//		}
//		return kieSession;s
//	};

    @SuppressWarnings("unchecked")
    public BiFunction<KieSession, AtomicReference<List<RuleResponse>>, KieSession> getRuleResponse = (kieSession, ruleResponses) -> {

        log.info("Obtaining RuleResponse results from KieSession");
        Collection<Object> result = (Collection<Object>) kieSession.getObjects(new ObjectFilter() {

            @Override
            public boolean accept(Object obj) {
                if (obj instanceof RuleResponse) {
                    return true;
                }
                return false;
            }
        });

        if (!result.isEmpty()) {

            List<RuleResponse> resultList = new ArrayList<RuleResponse>();
            result.stream().forEach(o -> resultList.add((RuleResponse) o));

            log.debug("{} RuleResponse results obtained from KieSession", resultList.size());
            ruleResponses.set(resultList);
        }

        return kieSession;
    };
}

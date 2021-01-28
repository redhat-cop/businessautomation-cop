package org.redhat.services.test.rules;

import org.junit.Test;
import org.redhat.services.model.RuleResponse;
import org.redhat.services.rules.api.RuleExecutor;
import org.redhat.services.test.AppTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RuleExecutorTest extends AppTestBase {

    @Autowired
    RuleExecutor ruleExecutor;

    @Test
    public void testHelloWorldRules(){
        RuleResponse ruleResponse = ruleExecutor.executeHelloWorldRules();
        assertThat(ruleResponse, notNullValue());
        assertThat(ruleResponse.getExecutionReference(), notNullValue());
        assertThat(ruleResponse.getPayload(), instanceOf(String.class));
        assertThat(ruleResponse.getPayload().toString(), equalTo("Hello World"));
        assertThat(ruleResponse.getRulesFired(), equalTo(1));
    }

    @Test
    public void testGoodByeRules(){
        String name = "Kris";

        RuleResponse ruleResponse = ruleExecutor.executeGoodbyeRules("Kris");
        assertThat(ruleResponse, notNullValue());
        assertThat(ruleResponse.getExecutionReference(), notNullValue());
        assertThat(ruleResponse.getPayload(), instanceOf(String.class));
        assertThat(ruleResponse.getPayload().toString(), equalTo("Goodbye " + name));
        assertThat(ruleResponse.getRulesFired(), equalTo(1));
    }
}

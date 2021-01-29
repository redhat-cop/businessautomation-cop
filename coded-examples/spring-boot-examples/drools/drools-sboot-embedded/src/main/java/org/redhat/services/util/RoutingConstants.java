package org.redhat.services.util;

public class RoutingConstants {

    // API Routing API
    public static String API_BASE = "/api/";
    public static String SWAGGER_ENDPOINT = "api-doc";
    public static String DEMO_ENDPOINT = "demo/rules/";
    public static String AUDIT_ENDPOINT = "audit/rules/";

    // Camel Endpoint Types
    public static final String DIRECT = "direct:";
    public static final String SEDA = "seda:";
    public static final String MOCK = "mock:";


    public interface AGENDA_GROUPS {
        public static final String DEMO_HELLO = "hello";
        public static final String DEMO_GOODBYE = "goodbye";
    }

    public interface ROUTE_ID {
        public static final String DEMO_API_ROUTE_HELLO = "demoRules.api." + CORE.HELLO_RULES;
        public static final String DEMO_API_ROUTE_GOODBYE = "demoRules.api." + CORE.GOODBYE_RULES;
        public static final String DEMO_ROUTE_HELLO = "demoRules.impl." + CORE.HELLO_RULES;
        public static final String DEMO_ROUTE_GOODBYE = "demoRules.impl." + CORE.GOODBYE_RULES;
        public static final String AUDIT_API_ROUTE_BYREF = "audit.api." + AUDIT.FIND_BY_REF;
        public static final String AUDIT_ROUTE_BYREF = "audit.impl." + AUDIT.FIND_BY_REF;
        public static final String AUDIT_ROUTE_PERSIST = "audit.impl." + AUDIT.EXECUTION_LOG;
        public static final String KIE_SCAN_KIEBASE = "kie.impl." + CORE.SCAN_KIEBASE;
        public static final String KIE_LOAD_KIEBASE = "kie.impl." + CORE.LOAD_KIEBASE;
    }

    public interface API {
        public static final String SWAGGER = API_BASE + SWAGGER_ENDPOINT;
        public static final String SCENARIO_LIST = API_BASE + "trigger/scenarios";
        public static final String DEMO_RULES_HELLOWORLD = DEMO_ENDPOINT + "hello";
        public static final String DEMO_RULES_GOODBYE = DEMO_ENDPOINT + "goodbye";
        public static final String AUDIT_LOG_BY_REFERENCE = AUDIT_ENDPOINT + "reference";
    }

    public interface CORE {
        public static final String SCAN_KIEBASE = DIRECT + "scanLatestKieBase";
        public static final String LOAD_KIEBASE = DIRECT + "loadKieBase";
        public static final String HELLO_RULES = DIRECT + "helloWorld";
        public static final String GOODBYE_RULES = DIRECT + "goodbyeWorld";
    }

    public interface AUDIT {
        public static final String EXECUTION_LOG = DIRECT + "persisExecutionLog";
        public static final String FIND_BY_REF = DIRECT + "executionLogByRef";
    }

    public interface CONTAINER_ID {
        public static final String DEMO = "DEMO_CONTAINER";
        public static final String MORTGAGES = "mortgages-1.0.0-SNAPSHOT";
    }
}
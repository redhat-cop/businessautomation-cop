package org.redhat.services.util;

public class RoutingConstants {

    // API Routing API
    public static String API_BASE = "/api/";
    public static String SWAGGER_ENDPOINT = "api-doc";
    public static String DEMO_ENDPOINT = "demo/rules/";

    // Camel Endpoint Types
    public static final String DIRECT = "direct:";
    public static final String SEDA = "seda:";
    public static final String MOCK = "mock:";
    public static final String ROUTE = "trigger.route.";


    public interface AGENDA_GROUPS {
        public static final String DEMO_HELLO = "hello";
        public static final String DEMO_GOODBYE = "goodbye";
    }

    public interface ROUTE_ID {
        public static final String DEMO_ROUTE_HELLO = ROUTE + "demoRules::" + CORE.HELLO_RULES;
        public static final String DEMO_ROUTE_GOODBYE = ROUTE + "demoRules::" + CORE.GOODBYE_RULES;
    }

    public interface API {
        public static final String SWAGGER = API_BASE + SWAGGER_ENDPOINT;
        public static final String SCENARIO_LIST = API_BASE + "trigger/scenarios";
        public static final String DEMO_RULES_HELLOWORLD = DEMO_ENDPOINT + "hello";
        public static final String DEMO_RULES_GOODBYE = DEMO_ENDPOINT + "goodbye";
    }

    public interface CORE {
        public static final String SCAN_KIEBASE = DIRECT + "scanLatestKieBase";
        public static final String LOAD_KIEBASE = DIRECT + "loadKieBase";
        public static final String HELLO_RULES = DIRECT + "helloWorld";
        public static final String GOODBYE_RULES = DIRECT + "goodbyeWorld";
    }
}
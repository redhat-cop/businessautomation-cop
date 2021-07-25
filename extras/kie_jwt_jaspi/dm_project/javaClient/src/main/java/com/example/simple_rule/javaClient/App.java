package com.example.simple_rule.javaClient;

/**
 * Invoke KIE Container
 */
public class App {

    public static void main(String[] args) {
  
        String url = "http://localhost:8080/kie-server/services/rest/server";
        String username = "kieServerUser";
        String password = "kieServerUser1234;";
        String containerId = "geo_location";
        String ksession = "restLess";

        KIEInvoker kie = new KIEInvoker();
        kie.invoke(url, username, password, ksession, containerId);
            
    }
}

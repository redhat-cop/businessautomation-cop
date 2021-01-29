package com.demo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;

public class UnitTestDMN {
    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kcontainer = ks.getKieClasspathContainer();
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(kcontainer.getKieBase())
                                                 .get(DMNRuntime.class);
        DMNModel dmnModel = dmnRuntime.getModels()
                                      .get(0);
        DMNContext context = dmnRuntime.newContext();

        context.set("Recent Transactions", initTransactions());
        context.set("Customer Context", initCustomerContext());

        DMNResult result = dmnRuntime.evaluateAll(dmnModel, context);
        // DMNResult result = dmnRuntime.evaluateByName(dmnModel, context, "Balance Trend");
        System.out.println(result);
    }

    private static Object initCustomerContext() {
        Map<String, Object> cc = new HashMap<>(); 
        cc.put("category", "gold");
        cc.put("monthly expense", -1450);
        cc.put("average balance", 3500);

        return cc;
    }

    public static List<Map<String, Object>> initTransactions() {

        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> t1 = new HashMap<>(); 
        t1.put("balance", 1500);
        t1.put("amount", -500);
        t1.put("value date", now);
        t1.put("category", "shopping");

        Map<String, Object> t2 = new HashMap<>(); 
        t2.put("balance", 2000);
        t2.put("amount", -200);
        t2.put("value date", now.minusMinutes(1));
        t2.put("category", "shopping");

        Map<String, Object> t3 = new HashMap<>(); 
        t3.put("balance", 3200);
        t3.put("amount", -1200);
        t3.put("value date", now.minusMinutes(2));
        t3.put("category", "shopping");

        List<Map<String, Object>> ts = Arrays.asList(t1, t2, t3);
        return ts;
    }
}

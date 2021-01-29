package com.redhat.summit2019.generator;

import com.redhat.summit2019.model.ImmutableLoan;
import com.redhat.summit2019.model.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LoanGenerator {

    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    String[] reasons;


    public LoanGenerator() {
        List<String> reasonsList = new ArrayList<>();

        reasonsList.add("Repair barn roof");
        reasonsList.add("New barn roof");
        reasonsList.add("Silo repair");
        reasonsList.add("Construction of new silo");
        reasonsList.add("Tractor maintenance");
        reasonsList.add("New fence");
        reasonsList.add("Fence repair");
        reasonsList.add("Build new barn");
        reasonsList.add("Clear up ditches");
        reasonsList.add("Build new ditches");
        reasonsList.add("Drill water bore");
        reasonsList.add("Purchase new tractor");
        reasonsList.add("Purchase new truck");
        reasonsList.add("Clear up asbestos");
        reasonsList.add("Repair glasshouse");
        reasonsList.add("Emergency water supply repairs");
        reasonsList.add("Purchase new cultivator");
        reasonsList.add("Purchase new seed drill");
        reasonsList.add("Trowel repair");
        reasonsList.add("Sprinkler system repair");

        reasons = reasonsList.toArray(new String[0]);
    }

    public Loan getLoan() {
        String reason = reasons[random.nextInt(reasons.length)];
        return ImmutableLoan.builder()
                .amount(random.nextLong(1000, 150000))
                .reason(reason)
                .build();
    }
}

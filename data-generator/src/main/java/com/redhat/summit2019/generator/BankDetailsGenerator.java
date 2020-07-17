package com.redhat.summit2019.generator;

import com.redhat.summit2019.model.BankDetails;
import com.redhat.summit2019.model.ImmutableBankDetails;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BankDetailsGenerator {
    private ThreadLocalRandom random = ThreadLocalRandom.current();
    private String names[];

    public BankDetailsGenerator() {
        List<String> namesList = new ArrayList<>();

        namesList.add("Adam and Company");
        namesList.add("Bank of Scotland");
        namesList.add("Clydesdale Bank");
        namesList.add("HBOS");
        namesList.add("Royal Bank of Scotland");

        names = namesList.toArray(new String[0]);
    }

    private long generateSortCode() {
        return Long.parseLong(new Formatter().format("%d%d%d",
                random.nextInt(10, 99),
                random.nextInt(10, 99),
                random.nextInt(10, 99))
                .toString());
    }

    private long generateAccountNumber() {
        return Long.parseLong(new Formatter().format("%d%d%d%d",
                random.nextInt(10, 99),
                random.nextInt(10, 99),
                random.nextInt(10, 99),
                random.nextInt(10, 99))
                .toString());
    }

    public BankDetails getBankDetails() {
        String name = names[random.nextInt(names.length)];

        return ImmutableBankDetails.builder()
                .name(name)
                .accountNumber(generateAccountNumber())
                .sortCode(generateSortCode())
                .build();
    }
}

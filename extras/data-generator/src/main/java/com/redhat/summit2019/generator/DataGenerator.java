package com.redhat.summit2019.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.summit2019.model.ImmutableLoanDetails;
import com.redhat.summit2019.model.Person;

import java.util.concurrent.ThreadLocalRandom;

public class DataGenerator {
    public String generateJsonData() throws JsonProcessingException {

        var personGenerator = new PersonGenerator();
        var farmGenerator = new FarmGenerator();
        var locationGenerator = new LocationGenerator();
        var bankDetailsGenerator = new BankDetailsGenerator();
        var loanGenerator = new LoanGenerator();
        var jsonWriter = new ObjectMapper();

        var random = ThreadLocalRandom.current();
        var randomGender = random.nextInt(3);

        Person person;
        switch (randomGender) {
            case 0:
                person = personGenerator.getPerson("f");
                break;
            case 1:
                person = personGenerator.getPerson("nb");
                break;
            case 2:
                person = personGenerator.getPerson("m");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + random);
        }

        return jsonWriter.writeValueAsString(ImmutableLoanDetails.builder()
                .person(person)
                .farm(farmGenerator.getFarm())
                .location(locationGenerator.getLocation())
                .bankDetails(bankDetailsGenerator.getBankDetails())
                .loan(loanGenerator.getLoan())
                .build());
    }
}

package com.redhat.summit2019.generator;

import com.redhat.summit2019.model.ImmutablePerson;
import com.redhat.summit2019.model.Person;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class PersonGenerator {
    private NameGenerator nameGenerator = new NameGenerator();
    private ThreadLocalRandom random = ThreadLocalRandom.current();

    public Person getPerson(String gender) {
        if (gender == null || gender.isBlank()) {
            throw new RuntimeException("Gender can't be null or empty.");
        }

        gender = gender.toUpperCase(Locale.ENGLISH);

        String[] name;
        switch (gender) {
            case "M":
                name = nameGenerator.getMaleName().split(" ");
                break;
            case "F":
                name = nameGenerator.getFemaleName().split(" ");
                break;
            case "NB":
                name = nameGenerator.getNonBinaryName().split(" ");
                break;
            default:
                throw new RuntimeException("Gender choices are [F]emale, [M]ale or [NB] for Non-Binary/Genderqueer.");
        }

        return ImmutablePerson.builder()
                .givenName(name[0])
                .surname(name[1])
                .gender(gender)
                .age(random.nextInt(15, 99))
                .build();
    }
}
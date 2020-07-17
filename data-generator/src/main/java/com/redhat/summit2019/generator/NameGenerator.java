package com.redhat.summit2019.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NameGenerator {
    private final String[] maleFirstNames;
    private final String[] femaleFirstNames;
    private final String[] surnames;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public NameGenerator() {
        List<String> maleFirstNamesList = new ArrayList<>();
        List<String> femaleFirstNamesList = new ArrayList<>();
        List<String> surnamesList = new ArrayList<>();

        femaleFirstNamesList.add("Agnes");
        femaleFirstNamesList.add("Aileen");
        femaleFirstNamesList.add("Ava");
        femaleFirstNamesList.add("Barbara");
        femaleFirstNamesList.add("Bridget");
        femaleFirstNamesList.add("Claire");
        femaleFirstNamesList.add("Ella");
        femaleFirstNamesList.add("Emily");
        femaleFirstNamesList.add("Emma");
        femaleFirstNamesList.add("Grace");
        femaleFirstNamesList.add("Hannah");
        femaleFirstNamesList.add("Imogen");
        femaleFirstNamesList.add("Jane");
        femaleFirstNamesList.add("Jessica");
        femaleFirstNamesList.add("Kristine");
        femaleFirstNamesList.add("Lucy");
        femaleFirstNamesList.add("Margaret");
        femaleFirstNamesList.add("Natalie");
        femaleFirstNamesList.add("Olivia");
        femaleFirstNamesList.add("Sophia");
        femaleFirstNamesList.add("Susan");
        femaleFirstNames = femaleFirstNamesList.toArray(new String[0]);

        maleFirstNamesList.add("Aaron");
        maleFirstNamesList.add("Albert");
        maleFirstNamesList.add("Andrew");
        maleFirstNamesList.add("Brett");
        maleFirstNamesList.add("Bruce");
        maleFirstNamesList.add("Donald");
        maleFirstNamesList.add("Fergus");
        maleFirstNamesList.add("Gavin");
        maleFirstNamesList.add("Greg");
        maleFirstNamesList.add("Iain");
        maleFirstNamesList.add("James");
        maleFirstNamesList.add("John");
        maleFirstNamesList.add("Logan");
        maleFirstNamesList.add("Lucas");
        maleFirstNamesList.add("Michael");
        maleFirstNamesList.add("Oliver");
        maleFirstNamesList.add("Owen");
        maleFirstNamesList.add("Steven");
        maleFirstNamesList.add("Stuart");
        maleFirstNamesList.add("Thomas");
        maleFirstNamesList.add("William");
        maleFirstNames = maleFirstNamesList.toArray(new String[0]);

        surnamesList.add("Anderson");
        surnamesList.add("Brown");
        surnamesList.add("Campbell");
        surnamesList.add("Catan");
        surnamesList.add("Clark");
        surnamesList.add("Craig");
        surnamesList.add("Dalais");
        surnamesList.add("Gall");
        surnamesList.add("Gray");
        surnamesList.add("Kerr");
        surnamesList.add("MacDonald");
        surnamesList.add("Morgan");
        surnamesList.add("Muir");
        surnamesList.add("O'Cain");
        surnamesList.add("Reid");
        surnamesList.add("Ross");
        surnamesList.add("Shaw");
        surnamesList.add("Smith");
        surnamesList.add("Walker");
        surnamesList.add("Watson");
        surnamesList.add("Young");
        surnames = surnamesList.toArray(new String[0]);
    }

    public String getFemaleName() {
        int randomName = random.nextInt(femaleFirstNames.length);
        int randomSurname = random.nextInt(surnames.length);
        return femaleFirstNames[randomName] + " " + surnames[randomSurname];
    }

    public String getMaleName() {
        int randomName = random.nextInt(maleFirstNames.length);
        int randomSurname = random.nextInt(surnames.length);
        return maleFirstNames[randomName] + " " + surnames[randomSurname];
    }

    public String getNonBinaryName() {
        int randomInt = random.nextInt(2);

        if (randomInt == 0) return getMaleName();

        return getFemaleName();
    }
}

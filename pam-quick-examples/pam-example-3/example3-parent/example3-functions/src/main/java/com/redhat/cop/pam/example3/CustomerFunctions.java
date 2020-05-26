package com.redhat.cop.pam.example3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CustomerFunctions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerFunctions.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-d");

    public static boolean isUnderage(final String dateOfBirth){
        final long age = ChronoUnit.YEARS.between(LocalDate.parse(dateOfBirth, FORMATTER), LocalDate.now());
        LOGGER.info("Customer is: {}", (age < 18 ? "underage" : "adult"));
        return age < 18l;
    }
}

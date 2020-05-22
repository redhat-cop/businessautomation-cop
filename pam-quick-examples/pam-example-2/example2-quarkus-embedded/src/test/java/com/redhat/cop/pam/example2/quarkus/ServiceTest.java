package com.redhat.cop.pam.example2.quarkus;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import com.redhat.cop.pam.example2.CanOpenAccountResults;
import com.redhat.cop.pam.example2.Customer;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@QuarkusTest
public class ServiceTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final LocalDate localDate = LocalDate.now();

    @Test
    public void allowedCustomerTest() {
        final Customer customer = new Customer();
        customer.setName("Donald");
        customer.setSurname("Duck");
        customer.setDateOfBirth("1870-06-09");

        given()
                .contentType(ContentType.JSON)
                .body(customer)
                .post("/services/customer")
                .then()
                .statusCode(200)
                .extract()
                .response().equals(CanOpenAccountResults.ALLOW);
    }

    @Test
    public void notAllowedCustomerTest() {

        final Customer customer = new Customer();
        customer.setName("Born");
        customer.setSurname("Today");
        customer.setDateOfBirth(localDate.format(formatter));

        given()
                .contentType(ContentType.JSON)
                .body(customer)
                .post("/services/customer")
                .then()
                .statusCode(200)
                .extract()
                .response().equals(CanOpenAccountResults.NOT_ALLOW);
    }
}

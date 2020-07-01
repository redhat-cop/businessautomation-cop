package com.redhat.cop.pam.example1.springboot;

import com.redhat.cop.pam.example1.CanOpenAccountResults;
import com.redhat.cop.pam.example1.Customer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceTest {

    private static final String BASE_URL = "http://localhost:%s/dmn-example-1/services/customer";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final LocalDate localDate = LocalDate.now();

    @LocalServerPort
    int randomServerPort;

    @Test
    public void allowedCustomerTest() throws URISyntaxException {
        final RestTemplate restTemplate = new RestTemplate();
        final URI uri = new URI(String.format(BASE_URL, randomServerPort));

        final Customer customer = new Customer();
        customer.setName("Donald");
        customer.setSurname("Duck");
        customer.setDateOfBirth("1870-06-09");

        final ResponseEntity<CanOpenAccountResults> response = restTemplate.postForEntity(uri, new HttpEntity<>(customer), CanOpenAccountResults.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        final CanOpenAccountResults result = response.getBody();
        Assert.assertEquals(CanOpenAccountResults.ALLOW, result);
    }

    @Test
    public void notAllowedCustomerTest() throws URISyntaxException {
        final RestTemplate restTemplate = new RestTemplate();
        final URI uri = new URI(String.format(BASE_URL, randomServerPort));

        final Customer customer = new Customer();
        customer.setName("Born");
        customer.setSurname("Today");
        customer.setDateOfBirth(localDate.format(formatter));

        final ResponseEntity<CanOpenAccountResults> response = restTemplate.postForEntity(uri, new HttpEntity<>(customer), CanOpenAccountResults.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        final CanOpenAccountResults result = response.getBody();
        Assert.assertEquals(CanOpenAccountResults.NOT_ALLOW, result);
    }
}
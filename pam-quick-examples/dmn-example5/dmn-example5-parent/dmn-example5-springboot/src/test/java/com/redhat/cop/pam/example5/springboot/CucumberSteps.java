package com.redhat.cop.pam.example5.springboot;

import java.net.URI;
import java.util.List;

import com.redhat.cop.pam.example5.CanOpenAccountResults;
import com.redhat.cop.pam.example5.Customer;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CucumberSteps extends SpringConfiguration{

    private static final String BASE_URL = "http://localhost:%s/dmn-example-5/services/customer";

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private CucumberSharedState cucumberSharedState;
    
    @Given("A customer that want to open an account$")
    public void given(DataTable dataTable) throws Throwable {
        final Customer customer = dataTable
        .cells(0)
        .stream()
        .skip(1) 
        .map(fields -> new Customer(fields.get(0), fields.get(1), fields.get(2)))
        .findFirst().get();
        cucumberSharedState.setCustomer(customer);
    }

    @When("^the bank employee check if the customer can open the account$")
    public void when() throws Throwable {
        final RestTemplate restTemplate = new RestTemplate();
        final URI uri = new URI(String.format(BASE_URL, randomServerPort));
        final ResponseEntity<CanOpenAccountResults> response = restTemplate.postForEntity(uri, new HttpEntity<>(cucumberSharedState.getCustomer()), CanOpenAccountResults.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
        cucumberSharedState.setCanOpenAccountResults(response.getBody());
    }

    @Then("^the system '([^']*)' the agent to open the account$")
    public void then(CanOpenAccountResults expectedResult) throws Throwable {
        Assert.assertEquals(expectedResult, cucumberSharedState.getCanOpenAccountResults());
    }

}
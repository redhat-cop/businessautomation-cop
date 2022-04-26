package org.acme;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

public class CucumberStepDefinitions {

    private Response result;
    private Integer age;
    private Boolean previousIncidents;

    @Given("the drivers age is {int}")
    public void the_drivers_age_is(Integer age) {
        this.age = age;
    }

    @Given("no previous incidents")
    public void no_previous_incidents() {
        this.previousIncidents = false;
    }

    @Given("has previous incidents")
    public void has_previous_incidents() {
        this.previousIncidents = true;
    }

    @When("I execute the pricing model")
    public void i_execute_the_pricing_model() {
        result = given()
                .body("{ \"Age\": " + age + ", \"Previous incidents?\": " + previousIncidents + " }")
                .contentType(ContentType.JSON)
                .when()
                .post("/pricing");
    }

    @Then("I expect the base price to be {int}")
    public void i_expect_the_base_price_to_be(Integer basePrice) {
        result.then()
                .statusCode(200)
                .body("'Base price'", is(basePrice));
    }
}

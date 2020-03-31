package com.mycompany.app;

import com.mycompany.app.models.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mycompany.app.models.Account;
import com.mycompany.app.models.AuthLevel;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class StepDefinitions {
	ArrayList<User> users = new ArrayList<User>();
	ArrayList<Account> accounts = new ArrayList<Account>();
	ArrayList<Account> creds = new ArrayList<Account>();
	private boolean loginSuccess = false;
	private String userType = null;
	private int userAuthLevel = -1;
	private String balance = null;
	private String loginResponse = null;

	@Given("^The following users are valid$")
	public void the_following_users_are_valid(DataTable dtUsers) throws Exception {
		List<Map<String, String>> usersIn = dtUsers.asMaps(String.class, String.class);
		for (int i = 0; i < usersIn.size(); i++) {
			users.add(new User(usersIn.get(i).get("Username"), usersIn.get(i).get("Password"),
					usersIn.get(i).get("UserType")));
		}
	}

	@Given("^these accounts exist$")
	public void these_accounts_exist(DataTable dtAccs) throws Exception {
		List<Map<String, String>> usersIn = dtAccs.asMaps(String.class, String.class);
		for (int i = 0; i < usersIn.size(); i++) {
			int balance = Integer.parseInt(usersIn.get(i).get("Balance"));
			int level = Integer.parseInt(usersIn.get(i).get("AccountLevel"));
			accounts.add(new Account(usersIn.get(i).get("AccountNum"), level, balance));
		}
	}

	@Given("^I am a manager$")
	public void i_am_a_manager() throws Exception {
		userType = "manager";
		if (AuthLevel.AUTH_LEVEL.containsKey(userType)) {
			userAuthLevel = AuthLevel.AUTH_LEVEL.get(userType);
		}
		assertTrue(userAuthLevel >= 0);
	}

	@Given("^I am a supervisor$")
	public void i_am_a_supervisor() throws Exception {
		userType = "supervisor";
		if (AuthLevel.AUTH_LEVEL.containsKey(userType)) {
			userAuthLevel = AuthLevel.AUTH_LEVEL.get(userType);
		}
		assertNotNull("Auth Level OK ", userAuthLevel);
	}

	@Given("^I am a standard user$")
	public void i_am_a_normal_user() throws Exception {
		userType = "user";
		if (AuthLevel.AUTH_LEVEL.containsKey(userType)) {
			userAuthLevel = AuthLevel.AUTH_LEVEL.get(userType);
		}
		assertNotNull("Auth Level OK ", userAuthLevel);
	}

	@When("^I login with my credentials$")
	public void i_login_with_credentials(DataTable dtCreds) throws Exception {
		List<Map<String, String>> creds = dtCreds.asMaps(String.class, String.class);
		String username = creds.get(0).get("Username");
		String password = creds.get(0).get("Password");
		for (User u : users) {
			if (u.getUserType().equals(userType) && u.getUserName().equals(username)
					&& u.getPassword().equals(password)) {
				loginSuccess = true;
			}
		}
		assertEquals("Successful Login ", true, loginSuccess);
	}

	@When("^I login with with Username \"([^\"]*)\" and Password \"([^\"]*)\"$")
	public void i_login_with_with_Username_and_Password(String username, String password) throws Exception {

		this.loginResponse = "invalid credentials";
		for (User u : users) {
			if (u.getUserName().equals(username) && u.getPassword().equals(password)) {
				this.loginResponse = "user authorised";
				this.userType = u.getUserType();
			}
		}

	}

	@When("^I request balance for (.*) from the accounts API$")
	public void i_request_balance_for_from_the_accounts_API(String accNum) throws Exception {
		boolean accountExists = false;
		for (int j = 0; j < accounts.size(); j++) {
			// if account exists
			if (accounts.get(j).getAccountNum().equals(accNum)) {
				accountExists = true;
				if (accounts.get(j).getAccountLevel() <= userAuthLevel) {
					balance = String.valueOf(accounts.get(j).getBalance());
				} else {
					balance = "Not Authorised";
				}
				break;
			}

		}
		if (!accountExists) {
			balance = "Account Not Found";
		}
	}

	@Then("^I should get \"([^\"]*)\" as the login response$")
	public void i_should_get_as_the_login_response(String expectedLoginResponse) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		assertEquals("Check Balance reponse ", expectedLoginResponse, loginResponse);
	}

	@Then("^I should get (.*) as the balance request response$")
	public void i_should_get_as_the_response(String expectedBalance) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		assertEquals("Check Balance reponse ", expectedBalance, balance);
	}

	@Then("^My usertype should be \"([^\"]*)\"$")
	public void my_usertype_should_be(String expectedUserType) throws Exception {
		// Write code here that turns the phrase above into concrete actions
		assertEquals("Check User type ", expectedUserType, userType);
	}

}

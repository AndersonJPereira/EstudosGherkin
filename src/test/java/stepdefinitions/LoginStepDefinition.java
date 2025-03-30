package stepdefinitions;

import classes.LoginData;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import junit.framework.Assert;
import pages.LoginPage;
import utils.DriverManager;
import utils.JsonUtils;

public class LoginStepDefinition {

	LoginPage loginPage = new LoginPage(DriverManager.getDriver());
	LoginData.Credentials valid = JsonUtils.getLoginData().valid;
	LoginData.Credentials invalid = JsonUtils.getLoginData().invalid;
	
	@When("I log in using valid credentials")
	public void i_log_in_using_valid_credentials() {
		loginPage.fillCredentials(valid.username, valid.password);
	}
	
	@When("I log in using invalid credentials")
	public void i_log_in_using_invalid_credentials() {
		loginPage.fillCredentials(invalid.username, invalid.password);
	}
	
	@Then("the login is successful")
	public void the_login_is_successful() {
	  Assert.assertTrue(loginPage.getLoginMessage().contains(valid.message));
	}

	@Then("an error message is shown")
	public void an_error_message_is_shown() {
		  Assert.assertTrue(loginPage.getLoginMessage().contains(invalid.message));
	}

}

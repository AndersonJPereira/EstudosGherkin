package stepdefinitions;

import classes.LoginData.User;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import junit.framework.Assert;
import pages.LoginPage;
import utils.DriverManager;
import utils.JsonUtils;

public class LoginStepDefinition {

	LoginPage loginPage = new LoginPage(DriverManager.getDriver());
	User user;
	
	
	@When("I log in using valid credentials")
	public void i_log_in_using_valid_credentials() {
		user= JsonUtils.buscarUsuarioPorStatus("valid");
		loginPage.fillCredentials(user.username, user.password);
	}
	
	@When("I log in using invalid credentials")
	public void i_log_in_using_invalid_credentials() {
		user= JsonUtils.buscarUsuarioPorStatus("invalid");
		loginPage.fillCredentials(user.username, user.password);
	}
	
	@When("I click login")
	public void i_cick_login() {
		loginPage.clickLoginButton();
	}

	
	@Then("the login is successful")
	public void the_login_is_successful() {
	  Assert.assertTrue(loginPage.getLoginMessage().contains(user.message));
	}

	@Then("an error message is shown")
	public void an_error_message_is_shown() {
		  Assert.assertTrue(loginPage.getLoginMessage().contains(user.message));
	}

}

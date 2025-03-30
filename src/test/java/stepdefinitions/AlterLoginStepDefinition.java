package stepdefinitions;

import classes.LoginData;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import junit.framework.Assert;
import pages.LoginPage;
import utils.DriverManager;
import utils.JsonUtils;

public class AlterLoginStepDefinition {
	
	LoginPage loginPage = new LoginPage(DriverManager.getDriver());
	LoginData.Credentials valid = JsonUtils.getLoginData().valid;
	LoginData.Credentials invalid = JsonUtils.getLoginData().invalid;


	@When("I use {string} credentials")
	public void i_use_credential(String value) {
		if (value.equals("valid")){
			loginPage.fillCredentials(valid.username, valid.password);
		}
		else {
			loginPage.fillCredentials(invalid.username, invalid.password);
		}
	}

	@Then("the login result is {string}")
	public void the_login_result_is(String value) {

		String message = null;

		if (value.equals("success")) {
			message = valid.message;
		}
		else {
			message = invalid.message;
		}

		Assert.assertTrue(loginPage.getLoginMessage().contains(message));
	}
}

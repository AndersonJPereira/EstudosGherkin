package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import junit.framework.Assert;
import pages.LoginPage;
import classes.LoginData.User;
import utils.DriverManager;
import utils.JsonUtils;

public class AlterLoginStepDefinition {
	
	LoginPage loginPage = new LoginPage(DriverManager.getDriver());
	User user;

	@When("I use {string} credentials")
	public void i_use_credential(String value) {
		if (value.equals("valid")){
			user =JsonUtils.buscarUsuarioPorStatus("valid");
			loginPage.fillCredentials(user.username, user.password);
		}
		else {
			user =JsonUtils.buscarUsuarioPorStatus("invalid");
			loginPage.fillCredentials(user.username, user.password);
		}
	}

	@Then("the login result is {string}")
	public void the_login_result_is(String value) {
		loginPage.clickLoginButton();
		Assert.assertTrue(loginPage.getLoginMessage().contains(user.message));
	}
}

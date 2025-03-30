package stepdefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import utils.DriverManager;

public class CommonStepDefinitions {

	@Given("I am on the login page")
	public void i_am_on_the_login_page() {
	   DriverManager.getDriver().get("https://the-internet.herokuapp.com/login");
	}
	

	@After
	public void closeBrowser() {
		DriverManager.quitDriver();;
	}


}

package stepdefinitions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import utils.DriverManager;

public class CommonStepDefinition {

	@Given("I am on the login page")
	public void i_am_on_the_login_page() {
	   DriverManager.getDriver().get("https://the-internet.herokuapp.com/login");
	}
	
	
	@After
	public void closeBrowser() {
		DriverManager.quitDriver();
	}
	
	@AfterStep
	public void takeScreenShot(Scenario scenario) {
		
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		
		TakesScreenshot takesScreenshot = (TakesScreenshot) DriverManager.getDriver();
		byte[] screenshot = takesScreenshot.getScreenshotAs(OutputType.BYTES);
		scenario.attach(screenshot, "image/png", timestamp+"_"+scenario.getId()+"_"+scenario.getName()+"_evidence");
	}


}

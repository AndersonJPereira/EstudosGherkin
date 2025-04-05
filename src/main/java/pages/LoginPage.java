package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import classes.LoginData;
import utils.JsonUtils;

public class LoginPage extends BasePage {
	
	
	private By username = By.name("username");
	private By password = By.id("password");
	private By button = By.cssSelector("i.fa.fa-2x.fa-sign-in");
	private By loginMessage = By.cssSelector("div#flash");

	public LoginPage(WebDriver driver) {
		super(driver);
	}
	
    public void fillCredentials(String usernameValue, String passwordValue) {
    	sendKeys(username, usernameValue);
    	sendKeys(password, passwordValue);
    	
    }
    
    public void clickLoginButton() {
    	click (button);
    }

    public String getLoginMessage() {
    	return getText(loginMessage);
    }
}

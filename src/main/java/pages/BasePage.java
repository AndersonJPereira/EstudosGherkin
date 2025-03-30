package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {

	WebDriver driver;
	WebDriverWait wait;
	
	public BasePage (WebDriver driver) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}
	
	public WebElement findElementVisible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}
	
	public WebElement findElementClickable(By locator) {
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}
	
	public void sendKeys(By locator, String value) {
	    WebElement element =  findElementVisible(locator);
	    element.clear();
	    element.sendKeys(value);
	}
	
	public void click(By locator) {
	    WebElement element =  findElementClickable(locator);
	    element.click();
	}
	
	public String getText (By locator) {
	    return findElementVisible(locator).getText();
	}
}

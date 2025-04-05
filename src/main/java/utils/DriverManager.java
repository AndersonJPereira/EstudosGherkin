package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class DriverManager {
	
	private static WebDriver driver;

    public static WebDriver getDriver() {
        if (driver == null) {
        	EdgeOptions options = new EdgeOptions();
        	options.addArguments("--headless=new"); // Use "--headless" se estiver com Chrome < 109
        	options.addArguments("--disable-gpu"); // Necessário em ambientes Linux
        	options.addArguments("--window-size=1920,1080");
            driver = new EdgeDriver(options); // ou ChromeDriver, etc.
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

}

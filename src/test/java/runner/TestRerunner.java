package runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
   features = "@target/failed_scenarios.txt",
   glue = "stepdefinitions",
   plugin = {"pretty", "html:target/rerun-report.html", "json:target/rerun-report.json"},
   monochrome = true
)
public class TestRerunner {

}

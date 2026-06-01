package com.insider.ui.steps;

import com.insider.context.ScenarioContext;
import com.insider.ui.pages.JobListingPage;
import com.insider.utils.ConfigReader;
import com.insider.utils.WaitHelper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobApplicationSteps {

    private static final Logger logger = LoggerFactory.getLogger(JobApplicationSteps.class);

    private JobListingPage jobListingPage() {
        return ScenarioContext.get("jobListingPage", JobListingPage.class);
    }

    private WebDriver driver() {
        return ScenarioContext.get("driver", WebDriver.class);
    }

    @Step("Clicking Apply button on the first job")
    @When("Click Apply button on first job")
    public void clickApplyOnFirstJob() {
        logger.info("🖱  Clicking Apply button on the first job...");
        Assert.assertTrue(
            jobListingPage().isApplyButtonDisplayed(),
            "Apply button is not visible — cannot proceed with job application"
        );
        jobListingPage().clickApplyButton();

        WaitHelper waitHelper = new WaitHelper(driver());
        int leverTimeout = ConfigReader.getInstance().getPropertyAsInt("browser.lever.link.wait");
        waitHelper.waitForUrlContains("lever.co", leverTimeout);
        waitHelper.waitForPageLoad();

        String currentUrl = driver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("lever.co"),
            "Expected Lever job posting page but got: " + currentUrl);

        String pageTitle = driver().getTitle();
        Assert.assertFalse(pageTitle.trim().isEmpty(),
            "Lever job posting page did not load — page title is empty");
        Assert.assertFalse(currentUrl.contains("/apply"),
            "Landed on application form instead of job posting page: " + currentUrl);
        logger.info("✅ Lever job posting page opened — URL: {}, title: '{}'", currentUrl, pageTitle);
    }

    @Step("Verifying Lever application form page opened")
    @Then("Lever application form page opens")
    public void verifyLeverFormOpens() {
        logger.info("🔍 Verifying redirect to Lever application form...");
        WaitHelper waitHelper = new WaitHelper(driver());
        int leverTimeout = ConfigReader.getInstance().getPropertyAsInt("browser.lever.link.wait");

        waitHelper.waitForUrlContains("/apply", leverTimeout);
        waitHelper.waitForPageLoad();

        String currentUrl = driver().getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("lever.co"),
            "Expected Lever application page but got: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("/apply"),
            "URL does not point to Lever application form (missing '/apply'): " + currentUrl);

        // "Submit your application" h4 is the definitive indicator the form page loaded
        WebElement formHeading = waitHelper.waitForElementPresence(By.tagName("h4"), leverTimeout);
        Assert.assertNotNull(formHeading,
            "Lever apply form heading (h4) not found — form may not have loaded");
        String headingText = formHeading.getText().trim();
        Assert.assertFalse(headingText.isEmpty(),
            "Lever apply form h4 heading is empty — unexpected page state");

        logger.info("✅ Lever application form verified — URL: {}, heading: '{}'", currentUrl, headingText);
    }
}

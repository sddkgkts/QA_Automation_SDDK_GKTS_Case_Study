package com.insider.ui.steps;

import com.insider.context.ScenarioContext;
import com.insider.ui.pages.CareersPage;
import com.insider.ui.pages.HomePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CareersPageSteps {

    private static final Logger logger = LoggerFactory.getLogger(CareersPageSteps.class);

    private HomePage homePage() {
        return ScenarioContext.get("homePage", HomePage.class);
    }

    private CareersPage careersPage() {
        return ScenarioContext.get("careersPage", CareersPage.class);
    }

    @Step("Navigating to the Careers page")
    @When("Navigate to careers page")
    public void navigateToCareersPage() {
        logger.info("🌐 Navigating to Careers page...");
        homePage().navigateToCareers();
        Assert.assertTrue(careersPage().isCareersPageDisplayed(), "Careers page did not load!");
        logger.info("✅ Careers page loaded");
    }

    @Step("Selecting {string} team")
    @And("Select {string} team")
    public void selectTeamByName(String teamName) {
        logger.info("🔘 Selecting team: '{}'...", teamName);
        careersPage().selectTeamByName(teamName);
        int count = careersPage().getJobCount();
        Assert.assertTrue(
            count > 0,
            "No jobs loaded after selecting team '" + teamName + "'"
        );
        logger.info("✅ Team '{}' selected — {} job(s) loaded", teamName, count);
    }

    @Step("Verifying all jobs contain {string} in position")
    @Then("All jobs contain {string} position")
    public void validateAllJobsPosition(String expected) {
        logger.info("🔍 Verifying all jobs contain '{}' in position...", expected);
        Assert.assertTrue(
            careersPage().allJobsContainInField(expected, "position"),
            "Not all jobs contain '" + expected + "' in the position field!"
        );
        logger.info("✅ All jobs contain '{}' in position", expected);
    }

    @Step("Verifying all jobs contain {string} in department")
    @And("All jobs contain {string} department")
    public void validateAllJobsDepartment(String expected) {
        logger.info("🔍 Verifying all jobs contain '{}' in department...", expected);
        Assert.assertTrue(
            careersPage().allJobsContainInField(expected, "department"),
            "Not all jobs contain '" + expected + "' in the department field!"
        );
        logger.info("✅ All jobs contain '{}' in department", expected);
    }

    @Step("Verifying all jobs contain {string} in location")
    @And("All jobs contain {string} location")
    public void validateAllJobsLocation(String expected) {
        logger.info("🔍 Verifying all jobs contain '{}' in location...", expected);
        Assert.assertTrue(
            careersPage().allJobsContainInField(expected, "location"),
            "Not all jobs contain '" + expected + "' in the location field!"
        );
        logger.info("✅ All jobs contain '{}' in location", expected);
    }

    @Step("Verifying job count is greater than zero")
    @Then("The job count is greater than zero")
    public void verifyJobCountGreaterThanZero() {
        logger.info("🔍 Checking that job count > 0...");
        int count = careersPage().getJobCount();
        Assert.assertTrue(count > 0, "No jobs found after filtering! Count: " + count);
        logger.info("✅ {} job(s) found", count);
    }

    // ── Filter steps ────────────────────────────────────────────────────────

    @Step("Verifying all four filter dropdowns are visible")
    @Then("All four filter dropdowns are visible")
    public void verifyAllFilterDropdownsVisible() {
        logger.info("🔍 Checking all 4 filter dropdowns are visible...");
        Assert.assertTrue(
            careersPage().areAllFilterDropdownsVisible(),
            "Expected 4 filter dropdowns but not all are visible on the page!"
        );
        logger.info("✅ All four filter dropdowns are visible");
    }

    @Step("Applying {string} filter with option {string}")
    @When("Apply {string} filter with option {string}")
    public void applyFilterWithOption(String filterName, String optionText) {
        logger.info("🔘 Applying '{}' filter → '{}'...", filterName, optionText);
        careersPage().applyFilter(filterName, optionText);
        logger.info("✅ '{}' filter applied — option: '{}'", filterName, optionText);
    }

    @Step("Verifying URL contains filter parameter {string}")
    @Then("The URL contains filter parameter {string}")
    public void verifyUrlContainsFilterParam(String param) {
        logger.info("🔍 Checking URL contains '{}'...", param);
        String rawUrl = careersPage().getCurrentUrl();
        String decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
        Assert.assertTrue(
            decodedUrl.contains(param),
            "Expected URL to contain '" + param + "' but got (decoded): " + decodedUrl
        );
        logger.info("✅ URL contains '{}': {}", param, decodedUrl);
    }

    @Step("Resetting {string} filter to default")
    @When("Reset {string} filter to default")
    public void resetFilterToDefault(String filterName) {
        logger.info("🔄 Resetting '{}' filter to All...", filterName);
        careersPage().resetFilter(filterName);
        logger.info("✅ '{}' filter reset to default (All)", filterName);
    }

    @Step("Verifying URL does not contain {string}")
    @Then("The URL does not contain {string}")
    public void verifyUrlDoesNotContain(String param) {
        logger.info("🔍 Checking URL does not contain '{}'...", param);
        String rawUrl = careersPage().getCurrentUrl();
        String decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8);
        Assert.assertFalse(
            decodedUrl.contains(param),
            "Expected URL NOT to contain '" + param + "' but got (decoded): " + decodedUrl
        );
        logger.info("✅ URL correctly does not contain '{}'", param);
    }
}

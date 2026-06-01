package com.insider.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * CucumberTestRunner
 *
 * Runs Cucumber BDD scenarios via TestNG's AbstractTestNGCucumberTests.
 *
 * Glue packages:
 *   com.insider.hooks       — UIHooks, APIHooks  (@Before / @After)
 *   com.insider.ui.steps    — HomePageSteps, CareersPageSteps, JobApplicationSteps
 *   com.insider.api.steps   — PetCrudSteps, PetAssertionSteps
 *
 * Tag filter examples (pass via -Dcucumber.filter.tags="..."):
 *   @smoke
 *   @ui
 *   @api
 *   @regression
 *   @ui and @smoke
 *   not @skip
 */
@CucumberOptions(
    features = {"src/test/resources/features"},
    glue = {
        "com.insider.hooks",       // UIHooks, APIHooks
        "com.insider.ui.steps",    // HomePageSteps, CareersPageSteps, JobApplicationSteps
        "com.insider.api.steps"    // PetCrudSteps, PetAssertionSteps
    },
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json",
        "junit:target/cucumber-reports/cucumber.xml",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true,
    dryRun = false,
    publish = false,
    tags = ""
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    /**
     * DataProvider that enables parallel scenario execution.
     * Thread count is controlled by testng.xml thread-count attribute.
     */
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}

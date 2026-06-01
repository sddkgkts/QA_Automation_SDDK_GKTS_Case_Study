package com.insider.ui.steps;

import com.insider.context.ScenarioContext;
import com.insider.ui.pages.HomePage;
import io.cucumber.java.en.Given;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePageSteps {

    private static final Logger logger = LoggerFactory.getLogger(HomePageSteps.class);

    private HomePage homePage() {
        return ScenarioContext.get("homePage", HomePage.class);
    }

    @Step("Opening InsiderOne home page")
    @Given("Insider home page is opened")
    public void openInsiderHomepage() {
        logger.info("🌐 Navigating to InsiderOne home page...");
        homePage().navigateToHomePage();
        homePage().waitForFullPageLoadWithModules();
        Assert.assertTrue(
            homePage().areAllMainModulesLoaded(),
            "Home page did not fully load — modules not visible. URL: " + homePage().getCurrentUrl()
        );
        logger.info("✅ Home page opened and fully loaded");
    }
}

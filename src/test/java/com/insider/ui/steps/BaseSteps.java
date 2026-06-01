package com.insider.ui.steps;

import com.insider.context.ScenarioContext;
import com.insider.ui.pages.HomePage;
import io.cucumber.java.en.And;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSteps {

    private static final Logger logger = LoggerFactory.getLogger(BaseSteps.class);

    private HomePage homePage() {
        return ScenarioContext.get("homePage", HomePage.class);
    }

    @Step("Clicking {string} button")
    @And("Click {string} button")
    public void clickButton(String buttonText) {
        logger.info("🖱  Clicking '{}' button...", buttonText);
        homePage().clickButtonByText(buttonText);
        logger.info("✅ '{}' button clicked", buttonText);
    }
}

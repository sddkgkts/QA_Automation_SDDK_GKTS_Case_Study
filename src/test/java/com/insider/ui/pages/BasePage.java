package com.insider.ui.pages;

import com.insider.utils.WaitHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BasePage
 *
 * Abstract base class for all Page Object classes.
 * Initialises PageFactory element bindings and provides
 * shared navigation and utility methods.
 */
public abstract class BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    private static final By COOKIE_ACCEPT = By.cssSelector("#wt-cli-accept-all-btn, #wt-cli-accept-btn");

    protected final WebDriver driver;
    protected final WaitHelper wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
        logger.debug("Page object initialised: {}", this.getClass().getSimpleName());
    }

    public void navigateTo(String url) {
        logger.info("🌐 Navigating to: {}", url);
        driver.get(url);
        wait.waitForPageLoad();
        acceptCookiesIfPresent();
    }

    public void acceptCookiesIfPresent() {
        if (wait.tryClickIfVisible(COOKIE_ACCEPT)) {
            logger.info("✅ Cookie consent accepted");
        }
    }

    public void clickButtonByText(String buttonText) {
        By locator = By.xpath(
            "//a[normalize-space()='" + buttonText + "'] | " +
            "//button[normalize-space()='" + buttonText + "']"
        );
        wait.clickWithScroll(locator);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}

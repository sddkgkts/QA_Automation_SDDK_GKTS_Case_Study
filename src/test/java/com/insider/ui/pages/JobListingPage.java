package com.insider.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * JobListingPage
 *
 * Page Object for job listing interactions on Lever.
 */
public class JobListingPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(JobListingPage.class);
    private static final By JOB_POSTINGS = By.cssSelector("div.posting");
    private static final By APPLY_BUTTON = By.cssSelector(
        "div.posting .posting-apply a.posting-btn-submit, a.posting-btn-submit"
    );

    public JobListingPage(WebDriver driver) {
        super(driver);
    }

    public void clickApplyButton() {
        try {
            List<WebElement> jobs = driver.findElements(JOB_POSTINGS);
            if (jobs.isEmpty()) {
                throw new RuntimeException("No job postings found on the page");
            }

            WebElement firstJob = jobs.get(0);
            wait.scrollIntoView(firstJob);

            WebElement applyLink = firstJob.findElement(
                By.cssSelector(".posting-apply a.posting-btn-submit, a.posting-btn-submit")
            );
            String originalWindow = driver.getWindowHandle();
            wait.clickWithScroll(applyLink);
            logger.info("✅ Apply button clicked");

            switchToNewWindowIfOpened(originalWindow);
            wait.waitForPageLoad();

        } catch (Exception e) {
            logger.error("❌ Could not click Apply button: {}", e.getMessage());
            throw new RuntimeException("Apply button click failed", e);
        }
    }

    public boolean isApplyButtonDisplayed() {
        try {
            WebElement apply = wait.findDisplayedElementWithScroll(APPLY_BUTTON);
            return apply != null && apply.isDisplayed();
        } catch (Exception e) {
            logger.error("❌ Apply button not visible: {}", e.getMessage());
            return false;
        }
    }

    private void switchToNewWindowIfOpened(String originalWindow) {
        Set<String> windows = driver.getWindowHandles();
        if (windows.size() > 1) {
            for (String window : windows) {
                if (!window.equals(originalWindow)) {
                    driver.switchTo().window(window);
                    logger.info("✅ Switched to new browser tab");
                    break;
                }
            }
        }
    }
}

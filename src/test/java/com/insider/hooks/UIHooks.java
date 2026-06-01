package com.insider.hooks;

import com.insider.context.ScenarioContext;
import com.insider.ui.pages.CareersPage;
import com.insider.ui.pages.HomePage;
import com.insider.ui.pages.JobListingPage;
import com.insider.utils.DriverManager;
import com.insider.utils.ScreenshotUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UIHooks
 *
 * Cucumber lifecycle hooks for UI test scenarios.
 * Runs for scenarios tagged with {@code @ui} or {@code @end-to-end}.
 *
 * Responsibilities:
 *  - Initialize and quit WebDriver
 *  - Instantiate Page Objects and store them in ScenarioContext
 *  - Capture a screenshot on test failure
 *  - Clean up ScenarioContext after each scenario
 */
public class UIHooks {

    private static final Logger logger = LoggerFactory.getLogger(UIHooks.class);

    // ── @Before ──────────────────────────────────────────────────────────────

    /**
     * Runs before every scenario tagged {@code @ui}.
     * Initialises WebDriver and populates ScenarioContext with Page Objects.
     *
     * @param scenario the current Cucumber scenario
     */
    @Before(order = 10, value = "@ui or @end-to-end")
    public void setUpUI(Scenario scenario) {
        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  🟢 UI SCENARIO STARTING                         ║");
        logger.info("║  📋 {}", scenario.getName());
        logger.info("║  🏷  Tags: {}", scenario.getSourceTagNames());
        logger.info("╚══════════════════════════════════════════════════╝");

        // Clear any leftover state from a previous scenario
        ScenarioContext.clear();
        ScenarioContext.set(ScenarioContext.SCENARIO_NAME, scenario.getName());

        // Start WebDriver
        DriverManager driverManager = DriverManager.getInstance();
        driverManager.initDriver();
        WebDriver driver = driverManager.getDriver();

        // Store Page Objects in context so step classes can retrieve them
        ScenarioContext.set("driver",         driver);
        ScenarioContext.set("homePage",       new HomePage(driver));
        ScenarioContext.set("careersPage",    new CareersPage(driver));
        ScenarioContext.set("jobListingPage", new JobListingPage(driver));

        logger.info("✅ WebDriver started — Page Objects ready");
    }

    // ── @After ───────────────────────────────────────────────────────────────

    /**
     * Runs after every scenario tagged {@code @ui}.
     * Captures a screenshot on failure, quits the driver, and clears context.
     *
     * @param scenario the current Cucumber scenario
     */
    @After(order = 10, value = "@ui or @end-to-end")
    public void tearDownUI(Scenario scenario) {
        String status = scenario.isFailed() ? "FAILED ❌" : "PASSED ✅";
        logger.info("╔══════════════════════════════════════════════════╗");
        logger.info("║  🔴 UI SCENARIO FINISHED — {}", status);
        logger.info("║  📋 {}", scenario.getName());
        logger.info("╚══════════════════════════════════════════════════╝");

        WebDriver driver = ScenarioContext.get("driver", WebDriver.class);

        // Capture screenshot on failure
        if (scenario.isFailed() && driver != null) {
            try {
                byte[] screenshot = ScreenshotUtil.takeScreenshotAsBytes(driver);
                if (screenshot.length > 0) {
                    scenario.attach(screenshot, "image/png", "Failure screenshot — " + scenario.getName());
                }
                String filePath = ScreenshotUtil.takeScreenshot(driver, scenario.getName());
                logger.error("📸 Screenshot saved: {}", filePath);
            } catch (Exception e) {
                logger.warn("⚠  Could not capture screenshot: {}", e.getMessage());
            }
        }

        // Quit WebDriver
        DriverManager.getInstance().quitDriver();

        // Remove ThreadLocal context
        ScenarioContext.remove();
    }
}

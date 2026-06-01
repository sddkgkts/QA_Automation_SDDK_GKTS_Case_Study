package com.insider.ui.pages;

import com.insider.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);

    private static final By LOGO             = By.cssSelector(".header-logo a, .header-logo svg, a.footer-logo-wrapper");
    private static final By HERO             = By.cssSelector("section.homepage-hero, .homepage-hero, .insiderone-hero-banner-container");
    private static final By FOOTER_WE_ARE_HIRING = By.cssSelector("a[data-text=\"We're hiring\"]");
    private static final By FOOTER           = By.cssSelector("footer, .footer-logo-wrapper, #footer");
    private static final By CONTENT_SECTIONS = By.cssSelector(
        "section.homepage-hero, section, .elementor-section, main .insiderone-hero-banner-container"
    );

    private static final By[] MAIN_MODULES = {LOGO, HERO, FOOTER};

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void navigateToHomePage() {
        String url = ConfigReader.getInstance().getProperty("insider.home.page");
        logger.info("🌐 Opening InsiderOne home page: {}", url);
        navigateTo(url);
    }

    public void waitForFullPageLoadWithModules() {
        acceptCookiesIfPresent();
        wait.waitForPageLoad();
        wait.scrollToBottomAndWaitForLazyContent();

        for (By module : MAIN_MODULES) {
            wait.waitForElementToBeVisible(module);
            logger.info("✅ Module visible: {}", module);
        }

        int sectionCount = driver.findElements(CONTENT_SECTIONS).size();
        int minSections = ConfigReader.getInstance().getPropertyAsInt("homepage.min.sections");
        if (sectionCount < minSections) {
            throw new RuntimeException(
                "Not all homepage modules loaded. Expected at least "
                    + minSections + " sections, found " + sectionCount
            );
        }

        wait.scrollToTop();
        logger.info("✅ Homepage fully loaded — {} content sections detected", sectionCount);
    }

    public boolean areAllMainModulesLoaded() {
        try {
            for (By module : MAIN_MODULES) {
                if (wait.findDisplayedElementWithScroll(module) == null) {
                    return false;
                }
            }
            return driver.findElements(CONTENT_SECTIONS).size()
                >= ConfigReader.getInstance().getPropertyAsInt("homepage.min.sections");
        } catch (Exception e) {
            logger.error("❌ Homepage modules check failed: {}", e.getMessage());
            return false;
        }
    }

    public void navigateToCareers() {
        logger.info("🖱  Clicking 'We're hiring' footer link...");
        wait.clickWithScroll(FOOTER_WE_ARE_HIRING);
        wait.waitForUrlContains("careers");
        logger.info("✅ Navigated to Careers page via footer 'We're hiring' link");
    }

}

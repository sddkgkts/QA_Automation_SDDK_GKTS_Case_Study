package com.insider.ui.pages;

import com.insider.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * CareersPage
 *
 * Page Object for the InsiderOne Careers page and Lever job listings.
 */
public class CareersPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(CareersPage.class);

    private static final By OPEN_ROLES          = By.cssSelector("#open-roles, [id='open-roles']");
    private static final By JOB_POSTINGS    = By.cssSelector("div.posting");
    private static final By FILTER_WRAPPERS = By.cssSelector(".filter-button-wrapper");
    // Lever job card sub-selectors
    private static final String LEVER_TEAM_SELECTOR = "span.sort-by-team, .posting-category.team, [data-qa='posting-team']";
    private static final String LEVER_LOCATION_SELECTOR = "span.sort-by-location, span.location, .posting-category.location, [data-qa='posting-location']";

    private List<WebElement> jobItems;

    public CareersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isCareersPageDisplayed() {
        try {
            wait.waitForUrlContains("careers");
            acceptCookiesIfPresent();
            wait.waitForElementToBeVisible(OPEN_ROLES);
            logger.info("✅ Careers page confirmed — URL: {}", driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            logger.error("❌ Careers page did not load: {}", e.getMessage());
            return false;
        }
    }

    public boolean areAllFilterDropdownsVisible() {
        try {
            List<WebElement> filters = driver.findElements(FILTER_WRAPPERS);
            return filters.size() >= 4 && filters.stream().allMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            logger.error("❌ Filter dropdowns not visible: {}", e.getMessage());
            return false;
        }
    }

    public void applyFilter(String filterName, String optionText) {
        By wrapperLocator = By.cssSelector(filterAriaSelector(filterName));

        wait.clickWithScroll(wrapperLocator);
        wait.waitUntilCondition(
            wrapperLocator,
            el -> "true".equals(el.getAttribute("aria-expanded")),
            ConfigReader.getInstance().getPropertyAsInt("browser.explicit.wait")
        );

        WebElement wrapper = driver.findElement(wrapperLocator);
        List<WebElement> options = wrapper.findElements(By.cssSelector(".category-link"));
        WebElement target = options.stream()
            .filter(link -> link.getText().trim().replaceAll("\\s+", " ").equals(optionText))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Option '" + optionText + "' not found in '" + filterName + "' filter"
            ));

        wait.clickWithScroll(target);
        wait.waitForPageLoad();
        logger.info("✅ Filter '{}' set to '{}'", filterName, optionText);
    }

    public void resetFilter(String filterName) {
        applyFilter(filterName, "All");
        logger.info("✅ Filter '{}' reset to default", filterName);
    }

    private String filterAriaSelector(String filterName) {
        // "Location" and "Location type" share the "Location" prefix —
        // using the colon in the aria-label to disambiguate them.
        String qualifier = "Location".equals(filterName) ? "Location:" : filterName;
        return "[aria-label*='Filter by " + qualifier + "']";
    }

    public void selectTeamByName(String teamName) {
        acceptCookiesIfPresent();
        scrollToOpenRoles();

        By openPositionsLink = By.xpath(
            "//div[contains(@class,'insiderone-icon-cards-grid-item-content')]" +
            "[.//h3[normalize-space()='" + teamName + "']]" +
            "//a[contains(@class,'insiderone-icon-cards-grid-item-btn')]"
        );

        wait.waitForElementToBeVisible(openPositionsLink);
        wait.clickWithScroll(openPositionsLink);
        wait.waitForPageLoad();
        logger.info("✅ '{}' team open positions link clicked", teamName);
    }

    public boolean allJobsContainInField(String expected, String field) {
        refreshJobList();
        if (jobItems.isEmpty()) {
            logger.warn("⚠  Job list is empty — cannot validate '{}' in '{}'", expected, field);
            return false;
        }
        String[] terms = Arrays.stream(expected.split(","))
            .map(String::trim)
            .map(this::normalizeText)
            .toArray(String[]::new);
        boolean result = jobItems.stream().allMatch(job -> {
            String fieldText = normalizeText(getJobFieldText(job, field));
            return Arrays.stream(terms).anyMatch(fieldText::contains);
        });
        logger.info("All jobs '{}' contains any of {}: {}", field, Arrays.toString(terms), result);
        return result;
    }

    private String getJobFieldText(WebElement job, String field) {
        if ("position".equals(field)) return getPostingTitle(job);
        if ("department".equals(field)) return getSubElementText(job, LEVER_TEAM_SELECTOR);
        if ("location".equals(field)) return getPostingLocation(job);
        return getJobText(job);
    }

    public int getJobCount() {
        refreshJobList();
        return jobItems.size();
    }

    private void scrollToOpenRoles() {
        WebElement section = wait.waitForElementPresence(OPEN_ROLES);
        wait.scrollIntoView(section);
    }

    private void refreshJobList() {
        if (driver.getCurrentUrl().contains("lever.co")) {
            jobItems = driver.findElements(JOB_POSTINGS);
        } else {
            jobItems = driver.findElements(By.cssSelector(
                ".position-list-item, .job-item, [class*='job-item'], li.position-list-item, div.posting"
            ));
        }
        logger.debug("Job list refreshed — count: {}", jobItems.size());
    }

    private String getPostingTitle(WebElement job) {
        try {
            List<WebElement> titles = job.findElements(By.cssSelector("h5[data-qa='posting-name'], .posting-title h5"));
            if (!titles.isEmpty()) {
                return titles.get(0).getText();
            }
        } catch (Exception e) {
            logger.debug("Could not read posting title: {}", e.getMessage());
        }
        return getJobText(job);
    }

    private String getPostingLocation(WebElement job) {
        String text = getSubElementText(job, LEVER_LOCATION_SELECTOR);
        return text.isEmpty() ? getJobText(job) : text;
    }

    private String getSubElementText(WebElement parent, String cssSelector) {
        try {
            List<WebElement> elements = parent.findElements(By.cssSelector(cssSelector));
            if (!elements.isEmpty()) {
                return elements.get(0).getText();
            }
        } catch (Exception e) {
            logger.debug("Could not read sub-element '{}': {}", cssSelector, e.getMessage());
        }
        return "";
    }

    private String getJobText(WebElement job) {
        try {
            return job.getText();
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\u00A0', ' ')
            .replace('—', '-')
            .replace('İ', 'I')
            .replace('ı', 'i')
            .toLowerCase()
            .trim();
    }
}

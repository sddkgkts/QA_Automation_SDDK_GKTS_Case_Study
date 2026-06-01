package com.insider.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Predicate;

/**
 * WaitHelper
 *
 * Centralised Selenium explicit wait utility with scroll-to-find support.
 */
public class WaitHelper {

    private static final Logger logger = LoggerFactory.getLogger(WaitHelper.class);

    private final WebDriver driver;
    private final FluentWait<WebDriver> wait;
    private final FluentWait<WebDriver> shortWait;
    private final FluentWait<WebDriver> pageLoadWait;
    private final int scrollStepPx;

    public WaitHelper(WebDriver driver) {
        this.driver = driver;
        int timeoutSeconds    = ConfigReader.getInstance().getPropertyAsInt("browser.explicit.wait");
        int pageLoadSeconds   = ConfigReader.getInstance().getPropertyAsInt("browser.page.load.timeout");
        int pollMs            = ConfigReader.getInstance().getPropertyAsInt("browser.poll.interval");
        this.scrollStepPx     = ConfigReader.getInstance().getPropertyAsInt("browser.scroll.step.px");
        this.wait             = buildWait(timeoutSeconds, pollMs);
        this.shortWait        = buildWait(Math.min(3, timeoutSeconds), pollMs);
        this.pageLoadWait     = buildWait(pageLoadSeconds, 500);
    }

    private FluentWait<WebDriver> buildWait(int timeoutSeconds, int pollMs) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .pollingEvery(Duration.ofMillis(pollMs));
    }

    public WebElement waitForElementToBeVisible(WebElement element) {
        scrollIntoView(element);
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    public WebElement waitForElementToBeVisible(By locator) {
        return wait.until(visibleAfterScroll(locator));
    }

    public WebElement waitForElementToBeClickable(WebElement element) {
        scrollIntoView(element);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public WebElement waitForElementToBeClickable(By locator) {
        return wait.until(clickableAfterScroll(locator));
    }

    public WebElement waitForElementPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement waitForElementPresence(By locator, int timeoutSeconds) {
        return buildWait(timeoutSeconds, ConfigReader.getInstance().getPropertyAsInt("browser.poll.interval"))
            .until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public void waitForUrlContains(String urlFragment) {
        wait.until(ExpectedConditions.urlContains(urlFragment));
        logger.debug("✅ URL now contains: '{}'", urlFragment);
    }

    public void waitForUrlContains(String urlFragment, int timeoutSeconds) {
        buildWait(timeoutSeconds, ConfigReader.getInstance().getPropertyAsInt("browser.poll.interval"))
            .until(ExpectedConditions.urlContains(urlFragment));
        logger.debug("✅ URL now contains: '{}' (timeout: {}s)", urlFragment, timeoutSeconds);
    }

    public void waitForPageLoad() {
        pageLoadWait.until(webDriver -> {
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            return "complete".equals(js.executeScript("return document.readyState"));
        });
        logger.debug("✅ Page fully loaded");
    }

    /**
     * Scrolls progressively to the page bottom, triggering lazy-loaded content,
     * and waits until the document height stabilises.
     */
    public void scrollToBottomAndWaitForLazyContent() {
        waitForPageLoad();

        long previousHeight = -1;
        int stableRounds = 0;
        int maxScrolls = ConfigReader.getInstance().getPropertyAsInt("browser.full.scroll.max.steps");
        int pauseMs = ConfigReader.getInstance().getPropertyAsInt("browser.scroll.pause.ms");

        for (int i = 0; i < maxScrolls; i++) {
            long pageHeight = getPageHeight();
            long scrollY = getScrollY();
            long viewportHeight = getViewportHeight();
            boolean atBottom = scrollY + viewportHeight >= pageHeight - 20;

            if (atBottom && pageHeight == previousHeight) {
                stableRounds++;
                if (stableRounds >= 2) {
                    break;
                }
            } else if (pageHeight != previousHeight) {
                stableRounds = 0;
            }

            if (atBottom) {
                scrollTo(pageHeight);
            } else {
                scrollBy(0, scrollStepPx);
            }

            previousHeight = pageHeight;
            sleep(pauseMs);
        }

        scrollTo(getPageHeight());
        waitForPageLoad();
        logger.debug("✅ Scrolled to page bottom — lazy content loaded");
    }

    public void waitUntilCondition(By locator, Predicate<WebElement> condition, int timeoutSeconds) {
        FluentWait<WebDriver> customWait = buildWait(
            timeoutSeconds,
            ConfigReader.getInstance().getPropertyAsInt("browser.poll.interval")
        );
        customWait.until(webDriver -> {
            WebElement element = findDisplayedElementWithScroll(locator);
            return element != null && condition.test(element);
        });
    }

    public void clickWithScroll(WebElement element) {
        scrollIntoView(element);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public void clickWithScroll(By locator) {
        clickWithScroll(waitForElementToBeClickable(locator));
    }

    public void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior:'instant', block:'center', inline:'center'});",
            element
        );
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    public WebElement findDisplayedElementWithScroll(By locator) {
        WebElement inView = findDisplayedInViewport(locator);
        if (inView != null) {
            return inView;
        }

        long startY = getScrollY();
        scrollToTop();

        for (int i = 0; i < 12; i++) {
            inView = findDisplayedInViewport(locator);
            if (inView != null) {
                return inView;
            }
            scrollBy(0, scrollStepPx);
        }

        scrollToTop();
        for (int i = 0; i < 6; i++) {
            inView = findDisplayedInViewport(locator);
            if (inView != null) {
                return inView;
            }
            scrollBy(0, -scrollStepPx);
        }

        scrollTo(startY);
        for (int direction : new int[] {scrollStepPx, -scrollStepPx}) {
            for (int i = 0; i < 4; i++) {
                inView = findDisplayedInViewport(locator);
                if (inView != null) {
                    return inView;
                }
                scrollBy(direction, 0);
            }
        }

        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed()) {
                scrollIntoView(element);
                return element;
            }
        }
        return null;
    }

    public boolean tryClickIfVisible(By locator) {
        try {
            WebElement element = shortWait.until(webDriver -> findDisplayedInViewport(locator));
            if (element != null) {
                scrollIntoView(element);
                clickWithScroll(element);
                return true;
            }
        } catch (Exception e) {
            logger.debug("Optional element not clicked: {}", locator);
        }
        return false;
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("⚠  Sleep interrupted: {}", e.getMessage());
        }
    }

    private ExpectedCondition<WebElement> visibleAfterScroll(By locator) {
        return webDriver -> {
            WebElement element = findDisplayedElementWithScroll(locator);
            if (element == null) {
                return null;
            }
            scrollIntoView(element);
            return element.isDisplayed() ? element : null;
        };
    }

    private ExpectedCondition<WebElement> clickableAfterScroll(By locator) {
        return webDriver -> {
            WebElement element = findDisplayedElementWithScroll(locator);
            if (element == null || !element.isDisplayed() || !element.isEnabled()) {
                return null;
            }
            scrollIntoView(element);
            return element;
        };
    }

    private WebElement findDisplayedInViewport(By locator) {
        for (WebElement element : driver.findElements(locator)) {
            if (element.isDisplayed()) {
                return element;
            }
        }
        return null;
    }

    private void scrollBy(int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
    }

    private void scrollTo(long y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, arguments[0]);", y);
    }

    private long getScrollY() {
        Object value = ((JavascriptExecutor) driver).executeScript(
            "return window.pageYOffset || document.documentElement.scrollTop;"
        );
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private long getPageHeight() {
        Object value = ((JavascriptExecutor) driver).executeScript(
            "return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);"
        );
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private long getViewportHeight() {
        Object value = ((JavascriptExecutor) driver).executeScript(
            "return window.innerHeight || document.documentElement.clientHeight;"
        );
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}

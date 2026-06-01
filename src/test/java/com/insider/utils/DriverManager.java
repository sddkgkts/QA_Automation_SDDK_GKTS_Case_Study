package com.insider.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openqa.selenium.Dimension;
import java.time.Duration;

/**
 * DriverManager
 *
 * Singleton + ThreadLocal WebDriver manager.
 * Each parallel test thread receives its own isolated WebDriver instance.
 *
 * Usage:
 *   DriverManager.getInstance().initDriver();
 *   WebDriver driver = DriverManager.getInstance().getDriver();
 *   DriverManager.getInstance().quitDriver();
 */
public class DriverManager {

    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);

    private static final ThreadLocal<DriverManager> instanceHolder = new ThreadLocal<>();
    private WebDriver driver;

    private DriverManager() {}

    /**
     * Returns the DriverManager instance for the current thread.
     */
    public static DriverManager getInstance() {
        if (instanceHolder.get() == null) {
            logger.debug("Creating DriverManager for thread: {}", Thread.currentThread().getName());
            instanceHolder.set(new DriverManager());
        }
        return instanceHolder.get();
    }

    /**
     * Initialise WebDriver based on {@code browser.type} in config.properties.
     * Supported values: CHROME, FIREFOX, EDGE.
     */
    public void initDriver() {
        if (driver != null) {
            logger.warn("⚠  WebDriver already started for thread: {}", Thread.currentThread().getName());
            return;
        }

        String browserType = ConfigReader.getInstance().getProperty("browser.type").toUpperCase();
        logger.info("🌐 Starting WebDriver — Browser: {}, Thread: {}", browserType, Thread.currentThread().getName());

        switch (browserType) {
            case "CHROME":  driver = initChrome();  break;
            case "FIREFOX": driver = initFirefox(); break;
            case "EDGE":    driver = initEdge();    break;
            default:
                throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        }

        int implicitWait    = ConfigReader.getInstance().getPropertyAsInt("browser.implicit.wait");
        int pageLoadTimeout = ConfigReader.getInstance().getPropertyAsInt("browser.page.load.timeout");

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        if (isHeadless()) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else if (ConfigReader.getInstance().getPropertyAsBoolean("browser.window.maximize")) {
            driver.manage().window().maximize();
        }

        logger.info("✅ WebDriver started");
    }

    /**
     * Returns the WebDriver for the current thread, initialising it if needed.
     */
    public WebDriver getDriver() {
        if (driver == null) {
            logger.warn("⚠  Driver is null — calling initDriver()");
            initDriver();
        }
        return driver;
    }

    /**
     * Quits the WebDriver and removes the ThreadLocal instance.
     */
    public void quitDriver() {
        if (driver != null) {
            try {
                driver.quit();
                logger.info("✅ WebDriver closed");
            } catch (Exception e) {
                logger.error("❌ Error closing WebDriver: {}", e.getMessage());
            } finally {
                driver = null;
                instanceHolder.remove();
            }
        }
    }

    /**
     * Checks whether the WebDriver session is still active.
     *
     * @return true if the driver is running
     */
    public boolean isDriverRunning() {
        if (driver == null) return false;
        try {
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Browser initialisers (private) ────────────────────────────────────────

    private boolean isHeadless() {
        return ConfigReader.getInstance().getPropertyAsBoolean("browser.headless")
               || "true".equalsIgnoreCase(System.getenv("CI"));
    }

    private WebDriver initChrome() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--force-device-scale-factor=1");
        } else {
            options.addArguments("--start-maximized");
        }
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--disable-extensions",
            "--disable-blink-features=AutomationControlled",
            "--remote-allow-origins=*"
        );
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        return new ChromeDriver(options);
    }

    private WebDriver initFirefox() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless()) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }
        return new FirefoxDriver(options);
    }

    private WebDriver initEdge() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--force-device-scale-factor=1");
        } else {
            options.addArguments("--start-maximized");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        return new EdgeDriver(options);
    }
}

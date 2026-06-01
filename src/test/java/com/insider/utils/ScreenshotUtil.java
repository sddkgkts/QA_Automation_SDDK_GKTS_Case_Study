package com.insider.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ScreenshotUtil
 *
 * Utility class for capturing screenshots on test failure.
 * Screenshots are saved to {@code target/screenshots/} and
 * can also be returned as a byte array for embedding in Cucumber reports.
 */
public class ScreenshotUtil {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "target/screenshots/";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtil() {}

    /**
     * Capture a screenshot and save it to disk.
     *
     * @param driver       active WebDriver instance
     * @param scenarioName scenario name (used as the file name)
     * @return absolute path to the saved screenshot, or empty string on failure
     */
    public static String takeScreenshot(WebDriver driver, String scenarioName) {
        if (!ConfigReader.getInstance().getPropertyAsBoolean("screenshot.on.failure")) {
            return "";
        }
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String safeName  = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
            String filePath  = SCREENSHOT_DIR + safeName + "_" + timestamp + ".png";

            File src  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File(filePath);
            FileUtils.copyFile(src, dest);

            logger.info("📸 Screenshot saved: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("❌ Failed to save screenshot: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Capture a screenshot as a byte array (for embedding in Cucumber/Allure reports).
     *
     * @param driver active WebDriver instance
     * @return screenshot as byte array, or empty array on failure
     */
    public static byte[] takeScreenshotAsBytes(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("❌ Failed to capture screenshot as bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
}

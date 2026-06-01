package com.insider.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ReportListener
 *
 * TestNG listener that logs test suite and individual test results.
 * Registered in testng.xml under the {@code <listeners>} block.
 */
public class ReportListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(ReportListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🚀 TEST SUITE STARTED: {}", context.getName());
        logger.info("═══════════════════════════════════════════════════");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🏁 TEST SUITE FINISHED: {}", context.getName());
        logger.info("   ✅ Passed  : {}", context.getPassedTests().size());
        logger.info("   ❌ Failed  : {}", context.getFailedTests().size());
        logger.info("   ⏭  Skipped : {}", context.getSkippedTests().size());
        logger.info("═══════════════════════════════════════════════════");
    }

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("▶  TEST STARTED: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("✅ TEST PASSED:  {} ({} ms)", result.getName(), duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("❌ TEST FAILED:  {}", result.getName());
        if (result.getThrowable() != null) {
            logger.error("   Cause: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⏭  TEST SKIPPED: {}", result.getName());
    }
}

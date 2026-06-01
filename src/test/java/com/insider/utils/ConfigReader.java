package com.insider.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfigReader
 *
 * Singleton configuration reader.
 * Loads values from {@code src/test/resources/config.properties}.
 *
 * Priority order for each key:
 *   1. JVM system property  (-Dkey=value)
 *   2. Environment variable (CI/CD pipelines)
 *   3. config.properties file
 */
public class ConfigReader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    private static volatile ConfigReader instance;
    private static Properties properties;

    private ConfigReader() {
        loadProperties();
    }

    /**
     * Returns the singleton ConfigReader instance (double-checked locking).
     */
    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
            logger.info("✅ config.properties loaded from: {}", CONFIG_PATH);
        } catch (IOException e) {
            logger.error("❌ Failed to load config.properties from: {}", CONFIG_PATH, e);
            throw new RuntimeException("Config file not found: " + CONFIG_PATH, e);
        }
    }

    /**
     * Retrieve a configuration value as a String.
     *
     * @param key property key
     * @return property value (empty string if not found)
     */
    public String getProperty(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) return systemValue;

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) return envValue;

        String propValue = properties.getProperty(key);
        if (propValue == null || propValue.isEmpty()) {
            logger.warn("⚠  Property not found: {}", key);
            return "";
        }
        return propValue.trim();
    }

    /**
     * Retrieve a configuration value as an int.
     *
     * @param key property key
     * @return integer value
     */
    public int getPropertyAsInt(String key) {
        String value = getProperty(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property '" + key + "' is not a valid integer: " + value, e);
        }
    }

    /**
     * Retrieve a configuration value as a boolean.
     *
     * @param key property key
     * @return boolean value
     */
    public boolean getPropertyAsBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * Retrieve a configuration value as a long.
     *
     * @param key property key
     * @return long value
     */
    public long getPropertyAsLong(String key) {
        String value = getProperty(key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property '" + key + "' is not a valid long: " + value, e);
        }
    }
}

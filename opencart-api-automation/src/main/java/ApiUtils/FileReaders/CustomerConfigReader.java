package ApiUtils.FileReaders;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CustomerConfigReader {

    private static Properties properties;
    private static final String configPath = "src/main/java/ApiConfig/CustomerConfig.Properties";

    // Load config.properties initially
    static {
        loadProperties();
    }

    // Method to load properties from config.properties
    private static void loadProperties() {
        try {
            properties = new Properties();
            FileInputStream input = new FileInputStream(configPath);
            properties.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.properties file.");
        }
    }

    // Get property value by key
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Reload properties dynamically before making API calls
    public static void reloadProperties() {
        loadProperties();  // Reload properties before reading them
        System.out.println("✅ Config properties reloaded successfully.");
    }
}
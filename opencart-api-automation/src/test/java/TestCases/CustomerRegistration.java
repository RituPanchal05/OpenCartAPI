package TestCases;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ApiUtils.FileReaders.CApiEpReader;
import ApiUtils.FileReaders.CustomerConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class CustomerRegistration {

    private String sessionCookie;
    private String configPath = System.getProperty("user.dir") + "/src/main/java/ApiConfig/CustomerConfig.Properties";
    private String cRegisterURL;

    @BeforeClass
    public void setup() {
        // Load base URL and config dynamically
        RestAssured.baseURI = CustomerConfigReader.getProperty("customerBaseURL");
        
        CustomerConfigReader.reloadProperties();
    }

    @Test(priority = 2, dataProvider = "customerData")
    public void registerCustomer(Map<String, String> customerData) {
        System.out.println("----------------------------Customer Registration---------------------------------------");
        
     // Build registration URL dynamically
        String cRegisterURL = RestAssured.baseURI + CApiEpReader.getEndpoint("customerRegister");

        
        // Send data using formParams()
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(customerData) 
                .log().all()
                .when()
                .post(cRegisterURL)
                .then()
                .log().all()
                .extract()
                .response();

        System.out.println(customerData);
        // Extract session cookie
        sessionCookie = response.getCookie("OCSESSID");

        if (sessionCookie != null && !sessionCookie.isEmpty()) {
            System.out.println("Session Cookie Captured: " + sessionCookie);

            // Update config.properties with session cookie
            Map<String, String> updates = Map.of(
                    "sessionCookie", sessionCookie
            );
            updateConfigFile(updates);
        } else {
            System.out.println("Failed to capture session cookie.");
        }
    }

    // ---------------------- Update Config Properties -----------------------
    public void updateConfigFile(Map<String, String> updates) {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(configPath)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("Failed to load config.properties file: " + e.getMessage());
            return;
        }

        // Update properties dynamically
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            System.out.println("Updating key: " + entry.getKey() + ", Value: " + entry.getValue());
            props.setProperty(entry.getKey(), entry.getValue());
        }

        // Save updated properties
        try (FileOutputStream out = new FileOutputStream(configPath)) {
            props.store(out, "Updated properties dynamically after login");
            System.out.println("Config properties updated successfully!");
        } catch (IOException e) {
            System.out.println("Failed to update config.properties file: " + e.getMessage());
        }
    }

    // ---------------------- DataProvider to read Excel data -----------------------
    @DataProvider(name = "customerData")
    public Object[][] getCustomerData() throws IOException {
        String excelFilePath = "/home/ritu.panchal@simform.dom/eclipse-workspace/opencart-api-automation/src/test/resources/TestData/LoginTestData.xlsx";
        FileInputStream fis = new FileInputStream(new File(excelFilePath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        int rowCount = sheet.getPhysicalNumberOfRows();
        int colCount = sheet.getRow(0) != null ? sheet.getRow(0).getPhysicalNumberOfCells() : 0;

        Object[][] data = new Object[rowCount - 1][1];  // Adjusting for Map<String, String>

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);

            if (row != null) {
                Map<String, String> customerData = new HashMap<>();
                for (int j = 0; j < colCount; j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = "";

                    if (cell != null) {
                        // Get the raw value and trim it
                        cellValue = cell.toString().trim();

                        // Special handling for email field (column index 2)
                        if (j == 2 && !isValidEmail(cellValue)) {
                            cellValue = "invalid-email";  // Set invalid emails to a default value
                        }

                        // Special handling for "agree" column (column index 6)
                        if (j == 6) {
                            if (!cellValue.equals("1") && !cellValue.equals("0")) {
                                cellValue = "0";  // Default invalid "agree" values to 0
                            }
                        }

                        // Remove special characters in the first and last names
                        if (j == 0 || j == 1) {  // First and Last name columns
                            cellValue = cellValue.replaceAll("[^\\x00-\\x7F]", "");  // Remove non-ASCII characters
                        }

                    }
                    // Add empty or sanitized value to the map with the corresponding header as key
                    String columnHeader = sheet.getRow(0).getCell(j).toString();
                    customerData.put(columnHeader, cellValue.isEmpty() ? "default-value" : cellValue);  // Handle empty cells by setting a default value
                }
                // Add the map to the data array
                data[i - 1][0] = customerData;
            } else {
                // Handle empty rows if necessary
                Map<String, String> emptyData = new HashMap<>();
                for (int j = 0; j < colCount; j++) {
                    emptyData.put(sheet.getRow(0).getCell(j).toString(), "default-value");
                }
                data[i - 1][0] = emptyData;  // Add default data for empty rows
            }
        }

        workbook.close();
        fis.close();
        return data;
    }

    private boolean isValidEmail(String email) {
        // Simple regex to check if email format is valid
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

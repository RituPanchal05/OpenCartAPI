package TestCases;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ApiUtils.FileReaders.CApiEpReader;
import ApiUtils.FileReaders.CustomerConfigReader;
import ApiUtils.FileReaders.jsonReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class trialRegi {

	private String sessionCookie;
	private String configPath = System.getProperty("user.dir") + "/src/main/java/ApiConfig/CustomerConfig.Properties";
	
	
    @BeforeClass
    public void setup() {
        
        // Load base URL and credentials from config.properties
        RestAssured.baseURI = CustomerConfigReader.getProperty("customerBaseURL");
        CustomerConfigReader.reloadProperties();
        
        sessionCookie = CustomerConfigReader.getProperty("sessionCookie");
    }

//------------------------------------- Customer Registration API ----------------------------------------

    @Test(priority = 1)
    public void registerCustomer() {

        System.out.println("-----------------------------Customer Registration---------------------------------------");

        // Read customer registration data from JSON file
        Map<String, String> customerData = jsonReader.getTestData("src/test/resources/TestData/customerRegistrationData.json");

        // Build registration URL dynamically
        String cRegisterURL = RestAssured.baseURI + CApiEpReader.getEndpoint("customerRegister");

        // Send data using formParams()
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParams(customerData)
                .cookie("OCSESSID", sessionCookie)
                .log().all()
                .when()
                .post("http://127.0.0.1/opencart/index.php?route=account/register")
                .then()
                .statusCode(200)
                .log().all()
                .extract()
                .response();  
        
        
        // Extract and session cookie
        sessionCookie = response.getCookie("OCSESSID");


        if (sessionCookie != null && !sessionCookie.isEmpty()) {
            System.out.println("Session Cookie Captured: " + sessionCookie);

            // Update config.properties with token and session cookie
            Map<String, String> updates = Map.of(
                    "sessionCookie", sessionCookie
            );
            updateConfigFile(updates);
        } else {
            System.out.println("Failed to capture session cookie.");
        }

//---------------------------------Call convertHtmlToJson()---------------------------------------------------
        
        
        
	     String htmlResponse = response.getBody().asString();
	     String jsonResponse = convertHtmlToJson(htmlResponse);
	     System.out.println("Converted JSON: " + jsonResponse);
	     
	     
//------------------------------------ ----Validations ----------------------------------------------

//	           Assertions.validate200Response(response);
   
    }
 

//----------------------------------Method to update config.properties dynamically--------------------------
    
    
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

        // Save the updated properties
        try (FileOutputStream out = new FileOutputStream(configPath)) {
            props.store(out, "Updated properties dynamically after login");
            System.out.println("Config properties updated successfully!");
        } catch (IOException e) {
            System.out.println("Failed to update config.properties file: " + e.getMessage());
        }
        
    }
    
   
//------------------------------------ HTML to JSON Conversion ------------------------------------

    public String convertHtmlToJson(String html) {
        Document document = Jsoup.parse(html);
        
        Element firstNameElement = document.selectFirst("input[name=firstname]");
        Element lastNameElement = document.selectFirst("input[name=lastname]");



        System.out.println("First Name Element: " + firstNameElement);
        System.out.println("Last Name Element: " + lastNameElement);


        // Prepare JSON manually with extracted data
        String jsonResponse = "{";
        if (firstNameElement != null) {
            jsonResponse += "\"firstname\": \"" + firstNameElement.attr("value") + "\"";
        }
        if (lastNameElement != null) {
            if (!jsonResponse.equals("{")) {
                jsonResponse += ", ";
            }
            jsonResponse += "\"lastname\": \"" + lastNameElement.attr("value") + "\"";
        }
        jsonResponse += "}";

        return jsonResponse;
    }
        
}


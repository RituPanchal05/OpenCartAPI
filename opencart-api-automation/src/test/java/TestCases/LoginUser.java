package TestCases;

import static io.restassured.RestAssured.given;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ApiUtils.FileReaders.ApiEpReader;
import ApiUtils.FileReaders.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class LoginUser {

    private String sessionCookie;
    private String authToken;
    private String configPath = System.getProperty("user.dir") + "/src/main/java/ApiConfig/Config.Properties";

    @BeforeClass
    public void setup() {
        // Set Base URI from config.properties
        RestAssured.baseURI = ConfigReader.getProperty("baseURL");
    }

    @Test(priority = 1)
    public void testLogin() {
        System.out.println("------------------OpenCart APIs--------------------------");

        // Build login URL dynamically from baseURL + endpoint
        String loginURL = ConfigReader.getProperty("baseURL") + ApiEpReader.getEndpoint("loginEndpoint");

        // API request to login endpoint
        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("user", "Default")
                .formParam("key", "kDINHV0vgNQykksHbVSpcI7Ypl0HJfVsjtG5j01jAI7do8vbURzCgsMWVd3mkEy3FK2ULyUBO3oili4tzjrNFYPSofCEfjbKwOZtxa11vKBkLtaVviGemsCWfF6MXjY4bWUYGr7CiiKISlS0Tw6PUEverIdBo2Fl8fPXG7MCC2sq0ACZZAwmFTfM8jLEhhsLk4gfNqlYf2QapnqiS80T8zvt1ULCpcTlv5t4DuTiQFVGh5xdQGAetOUH4ZULYQ3n")
                .log().all()
                .when()
                .post(loginURL)
                .then()
                .log().all()
                .extract()
                .response();

        // Validate 200 response
//        Assertions.validate200Response(response);

        // Extract API token and session cookie
        authToken = response.jsonPath().getString("api_token");
        sessionCookie = response.getCookie("OCSESSID");

        // Validate if token and cookie are not null
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: API token is missing in response.");
            return;
        }

        if (sessionCookie != null && !sessionCookie.isEmpty()) {
            System.out.println("Session Cookie Captured: " + sessionCookie);

            // Update config.properties with token and session cookie
            Map<String, String> updates = Map.of(
                    "authToken", authToken,
                    "sessionCookie", sessionCookie
            );
            updateConfigFile(updates);
        } else {
            System.out.println("Failed to capture session cookie.");
        }
    }

    // ------------------------------Method to update config.properties dynamically--------------------------
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
}


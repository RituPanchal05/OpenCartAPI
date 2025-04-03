package ApiBase;

import ApiUtils.FileReaders.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class base {

    protected static RequestSpecification request;
   
    // Setup before all tests
    public static void setup() {
        RestAssured.baseURI = ConfigReader.getProperty("baseURL");

        request = RestAssured
                .given()
                .header("Authorization", "Bearer " + ConfigReader.getProperty("authToken"))
                .contentType(ConfigReader.getProperty("contentType"));
    }
}
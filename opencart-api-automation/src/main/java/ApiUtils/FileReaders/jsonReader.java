package ApiUtils.FileReaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class jsonReader {

    // Read JSON file and convert to Map
    public static Map<String, String> getTestData(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> testData = null;

        try {
            testData = objectMapper.readValue(new File(filePath), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        }

        return testData;
    }
}


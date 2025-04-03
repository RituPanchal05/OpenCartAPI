
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class trialExcel {
    public static void main(String[] args) {
        String filePath = "/home/ritu.panchal@simform.dom/Downloads/RegistrationData.xlsx"; // Change to your file path
        readExcelFile(filePath);
    }

    public static void readExcelFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook = null;
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                System.out.println("Invalid file type. Please provide an Excel file.");
                return;
            }

            Sheet sheet = workbook.getSheetAt(0); // Reading the first sheet
            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t");                            break;
                        case BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        default:
                            System.out.print("\t");
                    }
                }
                System.out.println();
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

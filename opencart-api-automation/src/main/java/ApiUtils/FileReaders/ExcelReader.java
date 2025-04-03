package ApiUtils.FileReaders;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ExcelReader {
    public static Object[][] getExcelData(String filePath, String sheetName) throws IOException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        
        Iterator<Row> rowIterator = sheet.iterator();
        int rowCount = sheet.getPhysicalNumberOfRows();
        int colCount = sheet.getRow(0).getPhysicalNumberOfCells();
        
        Object[][] data = new Object[rowCount - 1][colCount];
        
        int rowIndex = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getRowNum() == 0) continue; // Skip header row
            for (int colIndex = 0; colIndex < colCount; colIndex++) {
                data[rowIndex][colIndex] = row.getCell(colIndex).toString();
            }
            rowIndex++;
        }
        
        workbook.close();
        fis.close();
        
        return data;
    }
}

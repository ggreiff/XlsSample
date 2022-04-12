package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.ResourceRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ggreiff on 5/25/2015.
 * XlsResourceHelper
 */
public class XlsxResourceHelper {

    final static Logger P6logger =  LogManager.getLogger(XlsxResourceHelper.class);

    public com.ggreiff.CommandArgs CommandArgs;

    public XlsxResourceHelper(CommandArgs commandArgs) {

        CommandArgs = commandArgs;
    }

    public List<ResourceRow> getResourceRows() {

        P6logger.info("Start getResourceRows");

        List<ResourceRow> ResourceRows = new ArrayList<>();
        try {
            String fileName = XlsxUtilityHelpers.xlsAbsoluteName(getClass().getResource(""), CommandArgs.getXlsxFileName());
            String sheetName = "Sheet1";
            if (!CommandArgs.getXlsxSheetName().isEmpty()) sheetName = CommandArgs.getXlsxSheetName();
            P6logger.info(String.format("Processing sheet %s on  xlsx file %s ",fileName, sheetName));

            FileInputStream fileInputStream = new FileInputStream(fileName);
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet worksheet = workbook.getSheet(sheetName);

            int failSafe = -1;
            while (worksheet != null) {
                if (++failSafe == 0) continue; //Header Row
                if (failSafe > worksheet.getLastRowNum()) break; // Exit on last row of the spreadsheet
                XSSFRow row = worksheet.getRow(failSafe);
                if (row == null) break;
                String resourceId = row.getCell(0).getStringCellValue().trim();
                if (resourceId.isEmpty()) break;
                String resourceName = row.getCell(1).getStringCellValue().trim();
                String resourceType = row.getCell(2).getStringCellValue().trim();
                String calendar = row.getCell(3).getStringCellValue().trim();
                Date effectiveDate = row.getCell(4).getDateCellValue();
                Double maxUnits = row.getCell(5).getNumericCellValue();
                Double priceUnit = row.getCell(6).getNumericCellValue();
                Double priceUnit2 = row.getCell(7).getNumericCellValue();
                Double priceUnit3 = row.getCell(8).getNumericCellValue();
                Double priceUnit4 = row.getCell(9).getNumericCellValue();
                Double priceUnit5 = row.getCell(10).getNumericCellValue();

                ResourceRows.add(new ResourceRow( resourceId, resourceName, resourceType,
                        calendar, effectiveDate, maxUnits,
                        priceUnit, priceUnit2, priceUnit3, priceUnit4, priceUnit5));
            }
            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        P6logger.info("Finished getResourceRows");
        return ResourceRows;
    }
}

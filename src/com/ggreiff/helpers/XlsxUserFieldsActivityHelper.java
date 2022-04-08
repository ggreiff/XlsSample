package com.ggreiff.helpers;

import com.ggreiff.rowdata.UserFieldActivityRow;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class XlsxUserFieldsActivityHelper {

    final static Logger P6logger = Logger.getLogger(XlsxUserFieldsActivityHelper.class);

    public com.ggreiff.CommandArgs CommandArgs;

    public XlsxUserFieldsActivityHelper(com.ggreiff.CommandArgs commandArgs) {

        CommandArgs = commandArgs;
    }

    public List<UserFieldActivityRow> getUserFieldsActivities() {

        P6logger.info("Start getUserFieldsActivities");

        List<UserFieldActivityRow> userFieldActivityList = new ArrayList<>();
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

                String projectId = row.getCell(0).getStringCellValue().trim();
                if (projectId.isEmpty()) break;
                String activityId = row.getCell(1).getStringCellValue().trim();
                String title = row.getCell(2).getStringCellValue().trim();
                String userType = row.getCell(3).getStringCellValue().trim();
                String userValue = row.getCell(4).getRawValue().trim();
                if (userType.equalsIgnoreCase("Text") || userType.equalsIgnoreCase("Indicator")) userValue = row.getCell(4).getStringCellValue().trim();

                userFieldActivityList.add(new UserFieldActivityRow(projectId, activityId, title, userType, userValue));
            }
            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        P6logger.info("Finished getUserFieldsActivities");
        return userFieldActivityList;
    }
}

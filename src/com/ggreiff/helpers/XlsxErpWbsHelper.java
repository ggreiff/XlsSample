package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.ErpWbsRow;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggreiff on 5/21/2015.
 * XlsWbsHelper
 */
public class XlsxErpWbsHelper {

    final static Logger P6logger = Logger.getLogger(XlsxErpWbsHelper.class);

    public CommandArgs CommandArgs;

    public XlsxErpWbsHelper(CommandArgs commandArgs) {
        CommandArgs = commandArgs;
    }

    public List<ErpWbsRow> getWbsRows() {

        P6logger.info("Start getWbsRows");

        List<ErpWbsRow> WbsList = new ArrayList<>();
        try {
            String fileName = XlsxUtilityHelpers.xlsAbsoluteName(getClass().getResource(""), CommandArgs.getXlsxFileName());
            String sheetName = "Sheet1";
            if (!CommandArgs.getXlsxSheetName().isEmpty()) sheetName = CommandArgs.getXlsxSheetName();
            P6logger.info(String.format("Processing sheet %s on  xlsx file %s ", fileName, sheetName));

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
                String activityName = row.getCell(2).getStringCellValue().trim();
                String activityType = row.getCell(3).getStringCellValue().trim();
                String wbsName = row.getCell(4).getStringCellValue().trim();
                String wbsNode = row.getCell(5).getStringCellValue().trim();

                WbsList.add(new ErpWbsRow(projectId, activityId, activityName, activityType, wbsName, wbsNode));
            }
            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        P6logger.info("Finished getWbsRows");
        return WbsList;
    }
}

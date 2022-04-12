package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.ActivityCodeValueRow;
import com.primavera.common.value.ObjectId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggreiff on 4/28/2015.
 * XlsActivityCodeHelper
 */
public class XlsxActivityCodeHelper {

    final static Logger P6logger =  LogManager.getLogger(XlsxActivityCodeHelper.class);

    public CommandArgs CommandArgs;

    public XlsxActivityCodeHelper(CommandArgs commandArgs) {
        CommandArgs = commandArgs;
    }

    public List<ActivityCodeValueRow> ActivityCodeValueList;

    public List<ActivityCodeValueRow> getProjectActivityCodesRows() {

        P6logger.info("Start getProjectActivityCodesRows");

        ActivityCodeValueList = new ArrayList<>();
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
                String activityCodeType = row.getCell(2).getStringCellValue().trim();
                String activityDescription = row.getCell(3).getStringCellValue().trim();
                String activityCodeValue = row.getCell(4).getStringCellValue().trim();
                String activityCodeNode = row.getCell(5).getStringCellValue().trim();
                ActivityCodeValueList.add(new ActivityCodeValueRow(projectId, activityId, activityCodeType, activityDescription, activityCodeValue, activityCodeNode));
            }

            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }

        P6logger.info("Finish getProjectActivityCodesRows");
        return ActivityCodeValueList;
    }

    public Map<String, ObjectId> getUniqueActivityCodesByProject(String projectId) {
        Map<String, ObjectId> retVal = new HashMap<>();
        for (ActivityCodeValueRow activityCodeValue : ActivityCodeValueList) {
            if (!activityCodeValue.getProjectID().equalsIgnoreCase(projectId)) continue;
            if (retVal.containsKey(activityCodeValue.getActivityCodeValue())) continue;
            retVal.put(activityCodeValue.getActivityCodeValue(), null);
        }
        return retVal;
    }
}

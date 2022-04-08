package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.ResourceSpreadRow;
import com.primavera.common.value.ObjectId;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.*;

public class XlsxResourceSpreadHelper {

    final static Logger P6logger = Logger.getLogger(XlsxResourceSpreadHelper.class);

    public com.ggreiff.CommandArgs CommandArgs;

    public XlsxResourceSpreadHelper(CommandArgs commandArgs) {
        CommandArgs = commandArgs;
    }

    public List<ResourceSpreadRow> ResourceSpreadList;

    public List<ResourceSpreadRow> getResourceSpreadRows() {

        P6logger.info("Start getResourceSpreadRows");

        ResourceSpreadList = new ArrayList<>();
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
                String resource =  row.getCell(2).getStringCellValue().trim();
                String unitType = row.getCell(3).getStringCellValue().trim();
                Double units = row.getCell(4).getNumericCellValue();
                Date periodDate = row.getCell(5).getDateCellValue();
                ResourceSpreadList.add(new ResourceSpreadRow(projectId, activityId, resource, unitType, units, periodDate));
            }

            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }

        P6logger.info("Finish getResourceSpreadRows");
        return ResourceSpreadList;
    }

    public Map<String, ObjectId> getUniqueResourceSpreadsByProject(String projectId) {
        Map<String, ObjectId> retVal = new HashMap<>();
        for (ResourceSpreadRow resourceSpreadRow : ResourceSpreadList) {
            if (!resourceSpreadRow.getProjectId().equalsIgnoreCase(projectId)) continue;
            retVal.put(resourceSpreadRow.getActivityId(), null);
        }
        return retVal;
    }
}

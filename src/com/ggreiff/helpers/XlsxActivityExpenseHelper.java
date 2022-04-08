package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.ActivityExpenseRow;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggreiff on 5/25/2015.
 * XlsActivityExpenseHelper
 */
public class XlsxActivityExpenseHelper {

    final static Logger P6logger = Logger.getLogger(XlsxActivityExpenseHelper.class);

    public com.ggreiff.CommandArgs CommandArgs;

    public XlsxActivityExpenseHelper(CommandArgs commandArgs) {

        CommandArgs = commandArgs;
    }

    public List<ActivityExpenseRow> getActivityExpenseRows() {

        P6logger.info("Start getActivityExpenseRows");

        List<ActivityExpenseRow> activityExpenseRows = new ArrayList<>();
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
                String expenseDescription = row.getCell(2).getStringCellValue().trim();
                String costAccountName = row.getCell(3).getStringCellValue().trim();
                String expenseCategoryName = row.getCell(4).getStringCellValue().trim();
                String accrualType = row.getCell(5).getStringCellValue().trim();
                String documentNumber = row.getCell(6).getStringCellValue().trim();
                Double plannedCost = row.getCell(7).getNumericCellValue();
                Double actualCost = row.getCell(8).getNumericCellValue();
                Double remainingCost = row.getCell(9).getNumericCellValue();
                Double pricePerUnit = row.getCell(10).getNumericCellValue();
                Double plannedUnits = row.getCell(11).getNumericCellValue();
                Double actualUnits = row.getCell(12).getNumericCellValue();
                Double remainingUnits = row.getCell(13).getNumericCellValue();
                String unitOfMeasure = row.getCell(14).getStringCellValue().trim();

                activityExpenseRows.add(new ActivityExpenseRow( projectId, activityId, expenseDescription, costAccountName,
                        expenseCategoryName, accrualType, documentNumber,
                        plannedCost, actualCost, remainingCost, pricePerUnit,
                        plannedUnits, actualUnits, remainingUnits, unitOfMeasure));
            }
            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        P6logger.info("Finished getWbsRows");
        return activityExpenseRows;
    }
}

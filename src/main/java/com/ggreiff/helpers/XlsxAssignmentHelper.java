
package com.ggreiff.helpers;

import com.ggreiff.CommandArgs;
import com.ggreiff.rowdata.AssignmentRow;
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
 * Created by ggreiff on 1/23/2015.
 * XlsAssignmentHelper
 */
public class XlsxAssignmentHelper {

    final static Logger P6logger =  LogManager.getLogger(XlsxAssignmentHelper.class);

    public com.ggreiff.CommandArgs CommandArgs;

    public XlsxAssignmentHelper(CommandArgs commandArgs) {
        CommandArgs = commandArgs;
    }

    public List<AssignmentRow> getAssignmentsRows() {

        P6logger.info("Start getAssignmentsRows");

        List<AssignmentRow> assignmentList = new ArrayList<>();
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
                String resourceId = row.getCell(2).getStringCellValue().trim();
                Double unitDouble = row.getCell(3).getNumericCellValue();
                String unitType = row.getCell(4).getStringCellValue().trim();
                Date periodDate = row.getCell(5).getDateCellValue();
                assignmentList.add(new AssignmentRow(projectId, activityId, resourceId, unitType, unitDouble, periodDate));
            }

            workbook.close();
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        P6logger.info("Finished getAssignmentsRows");
        return assignmentList;
    }
}



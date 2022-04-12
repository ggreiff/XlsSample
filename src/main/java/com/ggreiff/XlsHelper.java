
package com.ggreiff;

import com.ggreiff.p6.XlsxResourceSpread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggreiff on 1/23/2015.
 */
public class XlsHelper {

    final static Logger P6logger =  LogManager.getLogger(XlsHelper.class);

    public String XlsName;

    public XlsHelper(String xlsName) {
        XlsName = xlsName;
    }


    public List<Assignment> GetAssignments() {

        List<Assignment> assignmentList = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(xlsAbsoluteName(XlsName));
			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            XSSFSheet worksheet = workbook.getSheet("POI Worksheet");

            int failSafe = 0;
            while (true) {
                if (failSafe == 0) {
                    failSafe++;
                    continue; //Header Row
                }
                XSSFRow row = getRow(worksheet,failSafe);
                if (row == null) break;
                String projectId = row.getCell(0).getStringCellValue();
                if (projectId == null || projectId.isEmpty()) break;
                String activityId = row.getCell(1).getStringCellValue();
                String resourceId = row.getCell(2).getStringCellValue();
                Double unitDouble = row.getCell(3).getNumericCellValue();
                assignmentList.add(new Assignment(projectId, activityId, resourceId, unitDouble));
                if (failSafe++ > 100) break; // Need a better way to find the end of the spreadsheet
            }
            workbook.close();
        } catch (Exception ex) {
            P6logger.info(ex.getMessage());
        }
        return assignmentList;
    }

    public XSSFRow getRow(XSSFSheet worksheet, int rowNum) {
        try {
            return worksheet.getRow(rowNum);
        } catch (Exception ex) {
            return null;
        }

    }

    public String xlsAbsoluteName(String xlsName) {
        URL url = getClass().getResource("");
        return combine(url.getPath(), xlsName);
    }

    public static String combine(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

}

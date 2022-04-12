package com.ggreiff.helpers;

import java.io.File;
import java.net.URL;

/**
 * Created by ggreiff on 5/25/2015.
 * XlsxUtilityHelpers
 */
public class XlsxUtilityHelpers {

    public static String xlsAbsoluteName(URL url, String xlsName) {
        String path = url.getPath();
        int index = path.indexOf("/com");
        if (index <= 0) return xlsName;

        String executePath = path.substring(0, index);
        String absName = combinePath(executePath, xlsName);
        absName = absName.replace("\\bin\\", "\\resouces\\");
        return absName;
    }

    public static String combinePath(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }
}

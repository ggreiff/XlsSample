package com.ggreiff;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by ggreiff on 4/27/2015.
 *
 */
public interface  CommandArgs {

    @Option(shortName="x", longName = "XlsxFileName")
    String getXlsxFileName();

    @Option(shortName="s", longName = "XlsxSheetName")
    String getXlsxSheetName();

    @Option(shortName="u", longName = "P6User")
    String getP6User();

    @Option(shortName="p", longName = "P6Password")
    String getP6Password();

    @Option(shortName="d", longName = "P6Database")
    String getP6Database();

    @Option(shortName = "t", longName = "LoadType")
    String getLoadType();

    @Option(shortName ="b", longName = "BootStrapHome")
    String getBootStrapHome();

    @Option(helpRequest = true)
    boolean getHelp();

}

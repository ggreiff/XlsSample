package com.ggreiff;

import com.ggreiff.p6.*;
import com.lexicalscope.jewel.cli.CliFactory;
import com.primavera.integration.client.RMIURL;
import com.primavera.integration.client.Session;
import com.primavera.integration.common.DatabaseInstance;
import org.apache.log4j.*;

/**
 * Created by ggreiff on 4/28/2015.
 * MainProgram
 */
public class MainProgram {

    final static Logger P6logger = Logger.getLogger(MainProgram.class);

    public static void main(String[] args) {
        LogManager.getRootLogger().setLevel(Level.ERROR); // quite the P6 logger
        CommandArgs commandArgs;
        try {


            commandArgs = CliFactory.parseArguments(CommandArgs.class, args);
            StringBuilder msg = new StringBuilder();
            if (commandArgs.getP6User().isEmpty()) msg.append("A P6 User is required\n");
            if (commandArgs.getP6Password().isEmpty()) msg.append("A P6 User Password is required\n");
            if (commandArgs.getP6Database().isEmpty()) msg.append("A P6 Database Name is required\n");
            if (commandArgs.getXlsxFileName().isEmpty()) msg.append("A Excel Spreadsheet is required\n");
            if (commandArgs.getBootStrapHome().isEmpty())msg.append("To use the API the primavera.bootstrap.home must be set.\n");

            if (msg.length() > 0){
                P6logger.info(msg.toString());
                System.exit(-1);
            }
            String bootStrapHome =  commandArgs.getBootStrapHome();
            System.setProperty("primavera.bootstrap.home", bootStrapHome);

            //
            // See if we can log it
            //
            Session session;
            String user = commandArgs.getP6User();
            String password = commandArgs.getP6Password();
            String databaseName = commandArgs.getP6Database();
            String databaseId = "Unknown";

            DatabaseInstance[] dbInstances = Session.getDatabaseInstances(null);
            for (DatabaseInstance dbInstance : dbInstances) {
                if (dbInstance.getDatabaseName().equals(databaseName)) {
                    databaseId = dbInstance.getDatabaseId();
                    break;
                }
            }
            session = Session.login(RMIURL.getRmiUrl(RMIURL.LOCAL_SERVICE), databaseId, user, password);

            //
            // Add our appender to the P6 logger.
            //
            configureLogger();

            //
            // Do what the use asked
            //
            if (commandArgs.getLoadType().equalsIgnoreCase("activitycodes")) {
                P6logger.info("Starting activitycodes");
                XlsxActivityCodes xlsxActivityCodes = new XlsxActivityCodes();
                xlsxActivityCodes.run(session, commandArgs);
                P6logger.info("Finished activitycodes");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("assignments")) {
                P6logger.info("Starting assignments");
                XlsxAssignments xlsxAssignments = new XlsxAssignments();
                xlsxAssignments.run(session, commandArgs);
                P6logger.info("Finished assignments");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("erpwbs")) {
                P6logger.info("Starting erpwbs");
                XlsxErpWbs xlsxWbs = new XlsxErpWbs();
                xlsxWbs.run(session, commandArgs);
                P6logger.info("Finished erpwbs");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("activities")) {
                P6logger.info("Starting activities");
                XlsxWbsActivity xlsxWbsActivity = new XlsxWbsActivity();
                xlsxWbsActivity.run(session, commandArgs);
                P6logger.info("Finished activities");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("expenses")) {
                P6logger.info("Starting expenses");
                XlsxActivityExpenses xlsxActivityExpenses = new XlsxActivityExpenses();
                xlsxActivityExpenses.run(session, commandArgs);
                P6logger.info("Finished expenses");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("resources")) {
                P6logger.info("Starting resources");
                XlsxResources xlsxResources = new XlsxResources();
                xlsxResources.run(session, commandArgs);
                P6logger.info("Finished resources");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("resourcespreads")) {
                P6logger.info("Starting resourcespreads");
                XlsxResourceSpread xlsxResourceSpread = new XlsxResourceSpread();
                xlsxResourceSpread.run(session, commandArgs);
                P6logger.info("Finished resourcespreads");
            }

            if (commandArgs.getLoadType().equalsIgnoreCase("useractvityfields")) {
                P6logger.info("Starting useractvityfields");
                XlsxUserFieldActivity xlsxUserFieldActivity = new XlsxUserFieldActivity();
                xlsxUserFieldActivity.run(session, commandArgs);
                P6logger.info("Finished useractvityfields");
            }
        }
        catch(Exception ex)
        {
            P6logger.error(ex.getMessage());
        }
        LogManager.getRootLogger().setLevel(Level.ERROR); // quite the P6 logger
        System.exit(0);
    }

    public static void configureLogger(){

        //
        // configure the P6 logger for our use
        //
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);

        //
        // Configure a console appender and add it to the root logger
        //
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d %-5p [%c{1}] %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.FATAL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);

        //
        // Configure a file appender and add it to the root logger
        //
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("XlsSample.log");
        fa.setAppend(false);
        fa.setLayout(new PatternLayout(PATTERN));
        fa.activateOptions();
        Logger.getRootLogger().addAppender(fa);
    }
}

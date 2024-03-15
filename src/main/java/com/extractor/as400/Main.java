package com.extractor.as400;

import com.extractor.as400.enums.InstallationOptions;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.executors.ExecutorFactory;
import com.extractor.as400.executors.impl.InstallExecutor;
import com.extractor.as400.executors.impl.RunExecutor;
import com.extractor.as400.executors.impl.UninstallExecutor;
import com.extractor.as400.interfaces.IExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.UsageHelp;


public class Main {
    private static final String CLASSNAME = "MainClass";
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        final String ctx = CLASSNAME + ".main";
        logger.info("**************************************************************");
        logger.info("***** DATASOURCE AS400 VERSION " + ConfigVerification.API_VERSION + " *****");
        logger.info("**************************************************************");


        args = new String[]{"-option=INSTALL","-host=lo"};
        try {
            // Verify the args passed to the program -option=DEFAULT -host=localhost
            if (UsageHelp.argsVerificationOk(args)) {
                InstallationOptions opts = InstallationOptions.getByValue((String) UsageHelp.getParamsFromArgs().get("-option"));

                // Calling the correct executor for this option
                IExecutor iExecutor = new ExecutorFactory().getExecutor(opts);
                if (iExecutor != null) {
                    iExecutor.execute();
                } else {
                    throw new ExecutorAS400Exception ("Invalid value for param -option only INSTALL, UNINSTALL and RUN are allowed.");
                }
            } else {
                logger.info(UsageHelp.usage());
            }
        } catch (ExecutorAS400Exception e) {
            logger.error(ctx + ": " + e.getMessage());
        }
    }
}

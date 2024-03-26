package com.extractor.as400;

import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.enums.InstallationOptionsEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.executors.ExecutorFactory;
import com.extractor.as400.interfaces.IExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.UsageHelp;

import java.util.Arrays;


public class Main {
    private static final String CLASSNAME = "MainClass";
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        final String ctx = CLASSNAME + ".main";
        logger.info("**************************************************************");
        logger.info("***** DATASOURCE AS400 VERSION " + ConfigVerification.API_VERSION + " *****");
        logger.info("**************************************************************");

        try {
            // Verify the args passed to the program
            if (UsageHelp.argsVerification(args)) {
                InstallationOptionsEnum opts = InstallationOptionsEnum.getByValue((String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_OPTION.get()));

                // Calling the correct executor for this option
                IExecutor iExecutor = new ExecutorFactory().getExecutor(opts);
                if (iExecutor != null) {
                    ForwarderEnum forwarderEnum = ForwarderEnum.getByValue((String) UsageHelp.getParamsFromArgs().get(AllowedParamsEnum.PARAM_OUTPUT_FORWARDER.get()));
                    if (forwarderEnum!=ForwarderEnum.UNRECOGNIZED_FORWARDER) {
                        iExecutor.execute();
                    } else {
                        throw new ExecutorAS400Exception ("Invalid value for param "+ AllowedParamsEnum.PARAM_OUTPUT_FORWARDER.get() + ", only " + Arrays.toString(ForwarderEnum.getAllowedForwarders()) + " are allowed.");
                    }
                } else {
                    throw new ExecutorAS400Exception ("Invalid value for param "+ AllowedParamsEnum.PARAM_OPTION.get() + ", only " + Arrays.toString(InstallationOptionsEnum.getAllowedOptions()) + " are allowed.");
                }
            } else {
                logger.info(UsageHelp.usage());
            }
        } catch (ExecutorAS400Exception e) {
            logger.error(ctx + ": " + e.getMessage());
        }
    }
}

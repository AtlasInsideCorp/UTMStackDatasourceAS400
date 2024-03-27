package com.extractor.as400;

import agent.Common;
import com.extractor.as400.enums.AllowedParamsEnum;
import com.extractor.as400.enums.ForwarderEnum;
import com.extractor.as400.enums.InstallationOptionsEnum;
import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.executors.ExecutorFactory;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.jsonparser.GenericParser;
import com.extractor.as400.models.CollectorFileConfiguration;
import com.extractor.as400.models.ServerConfigAS400;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.util.CipherUtil;
import com.utmstack.grpc.jclient.config.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.extractor.as400.util.ConfigVerification;
import com.extractor.as400.util.UsageHelp;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
                        iExecutor.execute();
                } else {
                    throw new ExecutorAS400Exception ("Invalid value for param "+ AllowedParamsEnum.PARAM_OPTION.get() + ", only " + Arrays.toString(InstallationOptionsEnum.getAllowedOptions()) + " are allowed.");
                }
            } else {
                logger.info(UsageHelp.usage());
            }
        } catch (Exception e) {
            logger.error(ctx + ": " + e.getMessage());
        }
    }
}

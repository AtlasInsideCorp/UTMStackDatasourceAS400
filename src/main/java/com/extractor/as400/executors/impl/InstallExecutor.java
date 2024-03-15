package com.extractor.as400.executors.impl;

import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.interfaces.IExecutor;
import com.extractor.as400.util.UsageHelp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InstallExecutor implements IExecutor {
    private static final String CLASSNAME = "InstallExecutor";
    private static final Logger logger = LogManager.getLogger(InstallExecutor.class);
    /**
     * @throws ExecutorAS400Exception
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";

        // Begin gRPC connection
        String agentManagerHost = (String) UsageHelp.getParamsFromArgs().get("-host");

        logger.info(ctx + ": Success");
    }
}

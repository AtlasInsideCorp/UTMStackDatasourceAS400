package com.extractor.as400.executors.impl;

import com.extractor.as400.exceptions.ExecutorAS400Exception;
import com.extractor.as400.interfaces.IExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UninstallExecutor implements IExecutor {
    private static final String CLASSNAME = "UninstallExecutor";
    private static final Logger logger = LogManager.getLogger(UninstallExecutor.class);
    /**
     * @throws ExecutorAS400Exception
     */
    @Override
    public void execute() throws ExecutorAS400Exception {
        final String ctx = CLASSNAME + ".execute";
        logger.info(ctx + ": Success");
    }
}

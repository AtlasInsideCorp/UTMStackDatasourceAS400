package com.extractor.as400.util;

import com.extractor.as400.executors.impl.RunExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Freddy R. Laffita Almaguer.
 * Class used for utility methods related to threads
 */
public class ThreadsUtil {
    private static final String CLASSNAME = "ThreadsUtil";
    private static final Logger logger = LogManager.getLogger(RunExecutor.class);
    /**
     * Method used to stop the ThreadPoolExecutor
     * */
    public static void stopThreadExecutor (ThreadPoolExecutor executor) {
        final String ctx = CLASSNAME + ".stopThreadExecutor";
        //Thread end is called
        executor.shutdown();
        //Wait 1 sec until termination
        while (!executor.isTerminated()) {
            try {
                executor.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              logger.error(ctx + ": Thread pool executor was interrupted while shutdown -> "+e.getMessage());
            }
        }
    }
    /**
     * Method used to wait for an amount of seconds in the current thread
     * */
    public static void sleepCurrentThread (int timeInSeconds) {
        final String ctx = CLASSNAME + ".sleepCurrentThread";
        // Wait some time before begin again
        try {
            logger.info("Waiting " + timeInSeconds + " seconds before the next execution...");
            Thread.sleep(timeInSeconds * 1000L);
        } catch (Exception ie) {
            logger.error(ctx + ": Error while waiting for the next execution -> " + ie.getMessage());
        }
    }
}

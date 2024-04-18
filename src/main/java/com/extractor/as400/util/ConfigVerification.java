package com.extractor.as400.util;

import com.extractor.as400.config.InMemoryConfigurations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Freddy R. Laffita Almaguer.
 * This class is used to get the servers configuration from Servers.json file, create the list of as400 servers, perform some
 * tests and holds some utility methods used across the application
 */
public class ConfigVerification {
    private static final String CLASSNAME = "ConfigVerification";
    private static final Logger logger = LogManager.getLogger(ConfigVerification.class);
    // To hold the compilation version used to log
    public static final String API_VERSION_SHORT = "2.2.0";
    public static final String API_VERSION = API_VERSION_SHORT + " - 13-03-24 14:48:15";

    public static boolean isEnvironmentOk() {
        final String ctx = CLASSNAME + ".isEnvironmentOk";
        try {

            // Updating configuration and servers state list
            InMemoryConfigurations.updateConfigurationList();
            InMemoryConfigurations.generateServerStateList();
            if (InMemoryConfigurations.isServerConfigDuplicated()) {
                logger.error(ctx + ": Environment configuration error");
                logger.error(ctx + " *********** Check your configuration, some variables are not configured correctly ***********" +
                        "\n * Can't be duplicated servers -> (same hostname and tenant)");
                return false;
            }
            if (InMemoryConfigurations.getServerStateList().isEmpty()) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            logger.error(ctx + ": While reading servers configuration.");
            logger.error(UsageHelp.usage());
            try {
                Thread.sleep(30000);
                logger.error(ctx + ": We couldn't wait, the thread was interrupted");
            } catch (InterruptedException e) {
                return false;
            }
            return false;
        }
    }


    // Method to get current datetime
    public static String getActualDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }
}

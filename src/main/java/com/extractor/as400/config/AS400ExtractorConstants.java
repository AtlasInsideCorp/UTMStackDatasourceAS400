package com.extractor.as400.config;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to initialize some constants used across the api
 * */
public class AS400ExtractorConstants {
    // Every BATCH_SIZE logs the time mark is saved to file
    public static final int BATCH_SIZE = 100000;
    // Used as a key to identify as400 logs from third party tools
    public static final String META_AS400_KEY = "[DEVICE_TYPE=AS400, LOG_GEN_COLLECTOR=UTMStack] ";

    // Used for configuration set for COLLECTOR_MANAGER_HOST
    public static final String COLLECTOR_MANAGER_HOST = "CM_HOST";
    // Used for configuration set for COLLECTOR_MANAGER_PORT
    public static final String COLLECTOR_MANAGER_PORT = "CM_PORT";
    // Used for configuration set for COLLECTOR_LOGS_PORT
    public static final String COLLECTOR_LOGS_PORT = "CM_LOGS_PORT";
}

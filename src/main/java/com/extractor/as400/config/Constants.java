package com.extractor.as400.config;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used to initialize some constants used across the api
 * */
public class Constants {
    // Every BATCH_SIZE logs the time mark is saved to file
    public static final int BATCH_SIZE = 100000;
    // Used as a key to identify as400 logs from third party tools
    public static final String META_AS400_KEY = "[DEVICE_TYPE=AS400, LOG_GEN_AGENT=U7M_574-CK] ";
}

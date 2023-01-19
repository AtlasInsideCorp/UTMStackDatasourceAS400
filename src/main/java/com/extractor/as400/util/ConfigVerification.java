package com.extractor.as400.util;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.enums.EnvironmentsEnum;

public class ConfigVerification {
    public static boolean isEnvironmentOk() {
        if (EnvironmentConfig.AS400_HOST_NAME == null || EnvironmentConfig.AS400_HOST_NAME.compareTo("") == 0 ||
            EnvironmentConfig.AS400_USER_PASSWORD == null || EnvironmentConfig.AS400_USER_PASSWORD.compareTo("") == 0 ||
            EnvironmentConfig.AS400_USER_ID == null || EnvironmentConfig.AS400_USER_ID.compareTo("") == 0 ||
            EnvironmentConfig.SYSLOG_HOST == null || EnvironmentConfig.SYSLOG_HOST.compareTo("") == 0 ||
            EnvironmentConfig.SYSLOG_PORT == 0 || EnvironmentConfig.SYSLOG_PROTOCOL == null ||
            EnvironmentConfig.SYSLOG_PROTOCOL.compareTo("") == 0 ) {
            System.out.println("ConfigVerification.isEnvironmentOk(): Environment configuration error");
            System.out.println("\n *********** Check your environment configuration, some variables are not configured correctly ***********" +
                    "\n * " +
                    EnvironmentsEnum.AS400_HOST_NAME +
                    " is required, has to be defined and can't be empty" +
                    "\n * " +
                    EnvironmentsEnum.AS400_USER_ID +
                    " is required, has to be defined and can't be empty" +
                    "\n * " +
                    EnvironmentsEnum.AS400_USER_PASSWORD +
                    " is required, has to be defined and can't be empty" +
                    "\n * " +
                    EnvironmentsEnum.SYSLOG_HOST +
                    " is required, has to be defined and can't be empty" +
                    "\n * " +
                    EnvironmentsEnum.SYSLOG_PORT +
                    " is required, has to be defined and can't be empty or 0" +
                    "\n * " +
                    EnvironmentsEnum.SYSLOG_PROTOCOL +
                    " is required, has to be defined and can't be empty" +
                    "\n *********************************************************************************************************");
            return false;
        } else return true;
    }
}

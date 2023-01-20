package com.extractor.as400.connector.connectors;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.interfaces.IConnector;
import com.ibm.as400.access.AS400;

public class AS400Connector implements IConnector {
    private static AS400 as400;

    public AS400Connector() {
    }

    @Override
    public Object getConnector() throws Exception {

        if (as400 == null) {
            // Connect to the AS400 server
            as400 = new AS400(EnvironmentConfig.AS400_HOST_NAME);
            as400.setGuiAvailable(false);
            as400.setUserId(EnvironmentConfig.AS400_USER_ID);
            as400.setPassword(EnvironmentConfig.AS400_USER_PASSWORD.toCharArray());
            System.out.println("***** AS400 Initiated *****");
        } else {
            if (as400.isConnected() && as400.isConnectionAlive()) {
                System.out.println("***** AS400 connection reused *****");
            } else {
                as400 = new AS400(EnvironmentConfig.AS400_HOST_NAME);
                as400.setGuiAvailable(false);
                as400.setUserId(EnvironmentConfig.AS400_USER_ID);
                as400.setPassword(EnvironmentConfig.AS400_USER_PASSWORD.toCharArray());
                System.out.println("***** AS400 trying with new connection *****");
            }
        }
        return as400;
    }
}

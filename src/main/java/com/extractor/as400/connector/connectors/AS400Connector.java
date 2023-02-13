package com.extractor.as400.connector.connectors;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.interfaces.IConnector;
import com.extractor.as400.models.ServerDefAS400;
import com.ibm.as400.access.AS400;

public class AS400Connector implements IConnector {
    private static AS400 as400;
    private AS400 as400new;

    public AS400Connector() {
    }

    public Object getNewOrReuseConnector(ServerDefAS400 serverDefAS400) throws Exception {

        if (as400new == null) {
            // Connect to the AS400 server
            as400new = new AS400(serverDefAS400.getHostName());
            as400new.setGuiAvailable(false);
            as400new.setUserId(serverDefAS400.getUserId());
            as400new.setPassword(serverDefAS400.getUserPassword().toCharArray());
            // System.out.println("***** AS400 hostname -> "+serverDefAS400.getHostName()+", serverId -> "+serverDefAS400+" - (Initiated) *****");
        } else {
            if (as400new.isConnected() && as400new.isConnectionAlive()) {
                // System.out.println("***** AS400 hostname -> "+serverDefAS400.getHostName()+", serverId -> "+serverDefAS400+" - (reused) *****");
            } else {
                as400new = new AS400(serverDefAS400.getHostName());
                as400new.setGuiAvailable(false);
                as400new.setUserId(serverDefAS400.getUserId());
                as400new.setPassword(serverDefAS400.getUserPassword().toCharArray());
                // System.out.println("***** AS400 hostname -> "+serverDefAS400.getHostName()+", serverId -> "+serverDefAS400+" trying with new connection *****");
            }
        }
        return as400new;
    }

    // This method is for factory use, singleton as400 connection
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

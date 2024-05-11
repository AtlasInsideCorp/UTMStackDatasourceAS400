package com.extractor.as400.config;

import agent.CollectorOuterClass.CollectorConfig;
import com.extractor.as400.enums.ConfigurationParamsEnum;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.models.ServerState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InMemoryConfigurations {
    // To hold the configurations received by the stream from grpc server
    private static List<CollectorConfig> fromGrpcConfigurations = new ArrayList<>();
    // To make a copy of the configurations to be used to update the in memory list of servers.
    private static List<CollectorConfig> tempConfigurations = new ArrayList<>();
    // To hold the list of AS400 servers configuration
    private static List<ServerDefAS400> serversAS400 = new ArrayList<>();
    // To hold all servers with it state
    private static List<ServerState> serverStateList = new ArrayList<>();


    // Method to update the temporary configurations list
    public static void updateTemporaryConfigurationList() {
        tempConfigurations.clear();
        tempConfigurations.addAll(fromGrpcConfigurations);
    }

    // Method to generate the server list with state. Is static to always access the same elements when using threads
    public static void generateServerStateList() {
        serverStateList.clear();
        Iterator<ServerDefAS400> as400Iterator;
        for (as400Iterator = serversAS400.iterator(); as400Iterator.hasNext(); ) {
            ServerDefAS400 serverDefAS400 = as400Iterator.next();
            ServerState serverState = new ServerState(serverDefAS400);
            serverStateList.add(serverState);
        }
    }

    /***
     * Method to transform from list of CollectorConfig to list of ServerDefAS400
     */
    public static void updateConfigurationList() {
        CollectorConfig collectorConfig = !tempConfigurations.isEmpty() ?
                tempConfigurations.get(0) : null;
        if (collectorConfig != null) {
            serversAS400.clear();
            if (!collectorConfig.getGroupsList().isEmpty()) {
                collectorConfig.getGroupsList().forEach(
                        g -> {
                            ServerDefAS400 serverDefAS400 = new ServerDefAS400();
                            serverDefAS400.setTenant(g.getGroupName());
                            g.getConfigurationsList().forEach(
                                    c -> {
                                        if (c.getConfKey().equals(ConfigurationParamsEnum.AS400_USER.get())) {
                                            serverDefAS400.setUserId(c.getConfValue());
                                        }
                                        if (c.getConfKey().equals(ConfigurationParamsEnum.AS400_PASS.get())) {
                                            serverDefAS400.setUserPassword(c.getConfValue());
                                        }
                                        if (c.getConfKey().equals(ConfigurationParamsEnum.AS400_HOST.get())) {
                                            serverDefAS400.setHostName(c.getConfValue());
                                        }
                                    }
                            );
                            serversAS400.add(serverDefAS400);
                        }
                );
            }
        }
    }

    // Method to change the status of a ServerState instance
    public static void changeServerStateStatus(ServerState serverToChange, String newStatus) {
        Iterator<ServerState> as400Iterator;
        for (as400Iterator = serverStateList.iterator(); as400Iterator.hasNext(); ) {
            ServerState server = as400Iterator.next();
            if (server.getServerDefAS400().getHostName().compareToIgnoreCase(serverToChange.getServerDefAS400().getHostName()) == 0
                    && server.getServerDefAS400().getTenant().compareToIgnoreCase(serverToChange.getServerDefAS400().getTenant()) == 0) {
                server.setStatus(newStatus);
            }
        }
    }

    // Method to search the status of a ServerState instance
    public static ServerState getServerStateStatus(ServerState serverToChange) {
        Iterator<ServerState> as400Iterator;
        for (as400Iterator = serverStateList.iterator(); as400Iterator.hasNext(); ) {
            ServerState server = as400Iterator.next();
            if (server.getServerDefAS400().getHostName().compareToIgnoreCase(serverToChange.getServerDefAS400().getHostName()) == 0
                    && server.getServerDefAS400().getTenant().compareToIgnoreCase(serverToChange.getServerDefAS400().getTenant()) == 0) {
                return server;
            }
        }
        return null;
    }

    // Method to check if a server configuration is repeated (wrong config)
    public static boolean isServerConfigDuplicated() {
        for (int i = 0; i < serverStateList.size(); i++) {
            ServerState base = serverStateList.get(i);
            for (int j = 0; j < serverStateList.size(); j++) {
                if (i != j) {
                    ServerState search = serverStateList.get(j);
                    if (base.getServerDefAS400().getHostName().compareToIgnoreCase(search.getServerDefAS400().getHostName()) == 0
                            && base.getServerDefAS400().getTenant().compareToIgnoreCase(search.getServerDefAS400().getTenant()) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<CollectorConfig> getFromGrpcConfigurations() {
        return fromGrpcConfigurations;
    }

    public static List<ServerState> getServerStateList() {
        return serverStateList;
    }
}

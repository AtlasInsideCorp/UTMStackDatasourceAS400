package com.extractor.as400.util;

import com.extractor.as400.config.EnvironmentConfig;
import com.extractor.as400.enums.EnvironmentsEnum;
import com.extractor.as400.file.FileOperations;
import com.extractor.as400.jsonparser.GenericParser;
import com.extractor.as400.models.ServerConfigAS400;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.models.ServerState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigVerification {
    /**
     * This class is used to get the servers configuration from Servers.json file, create the list of as400 servers, perform some
     * tests and holds some utility methods used across the application
     */
    // To hold all AS400 servers config
    private static ServerConfigAS400 serverConfigAS400;
    // To hold all servers with it state
    private static List<ServerState> serverStateList = new ArrayList<>();
    // To hold the compilation version used to log
    public static final String API_VERSION = "2.1.0 - 04-04-23 10:29:14";

    public static boolean isEnvironmentOk() {
        if (EnvironmentConfig.SYSLOG_HOST == null || EnvironmentConfig.SYSLOG_HOST.compareTo("") == 0 ||
            EnvironmentConfig.SYSLOG_PORT == 0 || EnvironmentConfig.SYSLOG_PROTOCOL == null ||
            EnvironmentConfig.SYSLOG_PROTOCOL.compareTo("") == 0 ) {
            System.out.println("ConfigVerification.isEnvironmentOk(): Environment configuration error");
            System.out.println("\n *********** Check your environment configuration, some variables are not configured correctly ***********" +
                    "\n * /local_storage/Servers.json file is required, can't be empty and must be a valid JSON" +
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
        } else {
            try {
                // Parsing JSON Servers structure to handler class
                GenericParser gp = new GenericParser();
                serverConfigAS400 = gp.parseFrom(FileOperations.readServersFile(), ServerConfigAS400.class, new ServerConfigAS400());
                generateServerStateList();
                if (isServerConfigDuplicated()) {
                    System.out.println("ConfigVerification.isEnvironmentOk(): Environment configuration error");
                    System.out.println("\n *********** Check your environment configuration, some variables are not configured correctly ***********" +
                            "\n * In /local_storage/Servers.json, can't be duplicated servers -> (same hostname and serverId)");
                    return false;
                }
               return true;
            } catch (Exception ex) {
                System.out.println("ConfigVerification.isEnvironmentOk(): Environment configuration error");
                System.out.println("\n *********** Check your environment configuration, some variables are not configured correctly ***********" +
                        "\n * /local_storage/Servers.json file is required, can't be empty and must be a valid JSON" +
                        "\n * " +
                        EnvironmentsEnum.SYSLOG_HOST +
                        " is required, has to be defined and can't be empty" +
                        "\n * " +
                        EnvironmentsEnum.SYSLOG_PORT +
                        " is required, has to be defined and can't be empty or 0" +
                        "\n * " +
                        EnvironmentsEnum.SYSLOG_PROTOCOL +
                        " is required, has to be defined and can't be empty" +
                        "\n * Error: " + ex.getMessage() +
                        "\n *********************************************************************************************************");
                return false;
            }
        }
    }

    // Method to generate the server list with state. Is static to always access the same elements when using threads
    public static void generateServerStateList() {
        serverStateList.clear();
        Iterator<ServerDefAS400> as400Iterator;
        for (as400Iterator = serverConfigAS400.getServersAS400().iterator(); as400Iterator.hasNext();){
            ServerDefAS400 serverDefAS400 = as400Iterator.next();
            ServerState serverState = new ServerState(serverDefAS400);
            serverStateList.add(serverState);
        }
    }

    // Method to change the status of a ServerState instance
    public static void changeServerStateStatus(ServerState serverToChange, String newStatus) {
        Iterator<ServerState> as400Iterator;
        for (as400Iterator = serverStateList.iterator(); as400Iterator.hasNext();){
            ServerState server = as400Iterator.next();
            if (server.getServerDefAS400().getHostName().compareTo(serverToChange.getServerDefAS400().getHostName()) == 0
            && server.getServerDefAS400().getServerId().intValue() == serverToChange.getServerDefAS400().getServerId().intValue()) {
                server.setStatus(newStatus);
            }
        }
    }
    // Method to search the status of a ServerState instance
    public static ServerState getServerStateStatus(ServerState serverToChange) {
        Iterator<ServerState> as400Iterator;
        for (as400Iterator = serverStateList.iterator(); as400Iterator.hasNext();){
            ServerState server = as400Iterator.next();
            if (server.getServerDefAS400().getHostName().compareTo(serverToChange.getServerDefAS400().getHostName()) == 0
                    && server.getServerDefAS400().getServerId().intValue() == serverToChange.getServerDefAS400().getServerId().intValue()) {
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
                if (i!=j) {
                    ServerState search = serverStateList.get(j);
                    if (base.getServerDefAS400().getHostName().compareTo(search.getServerDefAS400().getHostName()) == 0
                            && base.getServerDefAS400().getServerId().intValue() == search.getServerDefAS400().getServerId().intValue()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Method to get current datetime
    public static String getActualDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }

    public static List<ServerState> getServerStateList() {
        return serverStateList;
    }
}

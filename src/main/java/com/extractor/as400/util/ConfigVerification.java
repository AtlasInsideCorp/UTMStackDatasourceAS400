package com.extractor.as400.util;

import com.extractor.as400.file.FileOperations;
import com.extractor.as400.jsonparser.GenericParser;
import com.extractor.as400.models.ServerConfigAS400;
import com.extractor.as400.models.ServerDefAS400;
import com.extractor.as400.models.ServerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Freddy R. Laffita Almaguer.
 * This class is used to get the servers configuration from Servers.json file, create the list of as400 servers, perform some
 * tests and holds some utility methods used across the application
 */
public class ConfigVerification {
    private static final String CLASSNAME = "ConfigVerification";
    private static final Logger logger = LogManager.getLogger(ConfigVerification.class);
    // To hold all AS400 servers config
    private static ServerConfigAS400 serverConfigAS400;
    // To hold all servers with it state
    private static List<ServerState> serverStateList = new ArrayList<>();
    // To hold the compilation version used to log
    public static final String API_VERSION_SHORT = "2.2.0";
    public static final String API_VERSION = API_VERSION_SHORT + " - 13-03-24 14:48:15";

    public static boolean isEnvironmentOk() {
        final String ctx = CLASSNAME + ".isEnvironmentOk";
            try {
                // Parsing JSON Servers structure to handler class
                GenericParser gp = new GenericParser();
                serverConfigAS400 = gp.parseFrom(FileOperations.readServersFile(), ServerConfigAS400.class, new ServerConfigAS400());
                generateServerStateList();
                if (isServerConfigDuplicated()) {
                    logger.error(ctx + ": Environment configuration error");
                    logger.error(ctx + " *********** Check your environment configuration, some variables are not configured correctly ***********" +
                            "\n * In /local_storage/Servers.json, can't be duplicated servers -> (same hostname and tenant)");
                    return false;
                }
               return true;
            } catch (Exception ex) {
                logger.error(ctx + ": While reading servers configuration file. Check that /local_storage/Servers.json file exists and is a valid json");
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
            if (server.getServerDefAS400().getHostName().compareToIgnoreCase(serverToChange.getServerDefAS400().getHostName()) == 0
            && server.getServerDefAS400().getTenant().compareToIgnoreCase(serverToChange.getServerDefAS400().getTenant())==0) {
                server.setStatus(newStatus);
            }
        }
    }
    // Method to search the status of a ServerState instance
    public static ServerState getServerStateStatus(ServerState serverToChange) {
        Iterator<ServerState> as400Iterator;
        for (as400Iterator = serverStateList.iterator(); as400Iterator.hasNext();){
            ServerState server = as400Iterator.next();
            if (server.getServerDefAS400().getHostName().compareToIgnoreCase(serverToChange.getServerDefAS400().getHostName()) == 0
                    && server.getServerDefAS400().getTenant().compareToIgnoreCase(serverToChange.getServerDefAS400().getTenant())==0) {
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
                    if (base.getServerDefAS400().getHostName().compareToIgnoreCase(search.getServerDefAS400().getHostName()) == 0
                            && base.getServerDefAS400().getTenant().compareToIgnoreCase(search.getServerDefAS400().getTenant()) == 0) {
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

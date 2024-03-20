package com.extractor.as400.models;


import agent.Common.AuthResponse;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used as VM to store the collector configuration
 * */
public class CollectorFileConfiguration {
    String hostCollectorManager;
    int portCollectorManager;
    int id;
    String key;

    public CollectorFileConfiguration(AuthResponse response, String hostCollectorManager, int portCollectorManager) {
        this.hostCollectorManager = hostCollectorManager;
        this.portCollectorManager = portCollectorManager;
        this.id = response.getId();
        this.key = response.getKey();
    }

    public CollectorFileConfiguration() {
    }

    public String getHostCollectorManager() {
        return hostCollectorManager;
    }

    public void setHostCollectorManager(String hostCollectorManager) {
        this.hostCollectorManager = hostCollectorManager;
    }

    public int getPortCollectorManager() {
        return portCollectorManager;
    }

    public void setPortCollectorManager(int portCollectorManager) {
        this.portCollectorManager = portCollectorManager;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

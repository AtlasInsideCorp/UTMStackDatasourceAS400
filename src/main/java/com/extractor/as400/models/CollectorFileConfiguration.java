package com.extractor.as400.models;


import agent.Common.AuthResponse;

/**
 * @author Freddy R. Laffita Almaguer
 * Class used as VM to store the collector configuration
 * */
public class CollectorFileConfiguration {
    String hostCollectorManager;
    int portCollectorManager;
    int portLogAuthProxy;
    int id;
    String key;

    public CollectorFileConfiguration(AuthResponse response,
                                      String hostCollectorManager,
                                      int portCollectorManager,
                                      int portLogAuthProxy) {
        this.hostCollectorManager = hostCollectorManager;
        this.portCollectorManager = portCollectorManager;
        this.id = response.getId();
        this.key = response.getKey();
        this.portLogAuthProxy = portLogAuthProxy;
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

    public int getPortLogAuthProxy() {
        return portLogAuthProxy;
    }

    public void setPortLogAuthProxy(int portLogAuthProxy) {
        this.portLogAuthProxy = portLogAuthProxy;
    }
}

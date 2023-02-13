package com.extractor.as400.models;

public class ServerDefAS400 {
    String hostname;
    String userId;
    String user_password;
    Integer serverId;

    public ServerDefAS400(String hostname, String userId, String user_password, Integer serverId) {
        this.hostname = hostname;
        this.userId = userId;
        this.user_password = user_password;
        this.serverId = serverId;
    }
    public ServerDefAS400(){}

    public String getHostName() {
        return hostname;
    }

    public void setHostName(String hostname) {
        this.hostname = hostname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return user_password;
    }

    public void setUserPassword(String user_password) {
        this.user_password = user_password;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "Server INFO { " +
                "serverId=" + serverId +
                ", hostname=" + hostname +
                " }";
    }
}

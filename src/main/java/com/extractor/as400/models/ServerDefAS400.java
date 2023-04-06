package com.extractor.as400.models;

public class ServerDefAS400 {
    String hostname;
    String userId;
    String user_password;
    Integer tenant;

    public ServerDefAS400(String hostname, String userId, String user_password, Integer tenant) {
        this.hostname = hostname;
        this.userId = userId;
        this.user_password = user_password;
        this.tenant = tenant;
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

    public Integer getTenant() {
        return tenant;
    }

    public void setTenant(Integer tenant) {
        this.tenant = tenant;
    }

    @Override
    public String toString() {
        return "Server INFO { " +
                "tenant=" + tenant +
                ", hostname=" + hostname +
                " }";
    }
}

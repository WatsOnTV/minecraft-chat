package com.watsontv.mcchat;

public class Player {
    String username;
    String uuid;
    String token;
    Player(String username){
        this.username = username;
    }
    Player(String username, String uuid, String token){
        this.username = username;
        this.uuid = uuid;
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getUsername() {
        return username;
    }
    public String getUuid() {
        return uuid;
    }
    public String getToken() {
        return token;
    }
}

package com.watsontv.mcchat;

import com.google.gson.JsonObject;
import com.watsontv.mcchat.packets.status.clientbound.PongPacket;
import com.watsontv.mcchat.packets.status.clientbound.ResponsePacket;
import com.watsontv.mcchat.packets.status.serverbound.PingPacket;
import com.watsontv.mcchat.packets.status.serverbound.RequestPacket;
import com.watsontv.mcchat.protocol.ProtocolStates;
import com.watsontv.mcchat.protocol.ProtocolVersions;

import java.io.*;

public class MCServerInfo {
    String host, serverDescription, versionName;
    int port, protocolVersion;
    JsonObject playerData;
    MCServerInfo(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.getInfo();
    }

    void getInfo() throws IOException {
        Connection con = new Connection(this.host, this.port);

        // We first have to handshake with the server
        con.makeHandshake(ProtocolStates.STATUS, ProtocolVersions.UNKNOWN);

        // Now we send a request packet to the server, indicating we only want the status
        RequestPacket requestPacket = new RequestPacket(con.shouldCompress, con.shouldEncrypt);
        con.sendPacket(requestPacket);
        requestPacket.close();

        // We can now read the response, and get the fields we want
        ResponsePacket responsePacket = (ResponsePacket) con.readPacket();
        this.protocolVersion = responsePacket.getProtocolVersion();
        //this.serverDescription = responsePacket.getServerDescription();
        this.versionName = responsePacket.getVersionName();
        this.playerData = responsePacket.getPlayerData();
        responsePacket.close();

        // Send a ping packet with the current time as the payload
        long payload = System.currentTimeMillis();
        PingPacket pingPacket = new PingPacket(con.shouldCompress, con.shouldEncrypt, payload);
        con.sendPacket(pingPacket);
        pingPacket.close();

        // And finally receive a pong and verify the payload is the same as the ping
        PongPacket pongPacket = (PongPacket) con.readPacket();
        if(pongPacket.getPayload() != payload){
            throw new IOException("The server sent an incorrect Pong");
        }
        pongPacket.close();

        // Clean up
        con.close();
    }

    public String getServerDescription(){
        return serverDescription;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public int getMaxPlayers() {
        return playerData.get("max").getAsInt();
    }

    public int getNumberOnlinePlayers(){
        return playerData.get("online").getAsInt();
    }

    public String getVersionName() {
        return versionName;
    }
}

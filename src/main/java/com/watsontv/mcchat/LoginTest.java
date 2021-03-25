package com.watsontv.mcchat;

import com.google.gson.JsonObject;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;
import com.watsontv.mcchat.packets.login.clientbound.EncryptionRequestPacket;
import com.watsontv.mcchat.packets.login.serverbound.EncryptionResponsePacket;
import com.watsontv.mcchat.packets.login.serverbound.LoginStartPacket;
import com.watsontv.mcchat.protocol.ProtocolStates;
import java.io.*;

public class LoginTest {
    String host;
    int port;
    String username;
    String password;
    boolean offlineMode;
    Player player;
    LoginTest(String host, int port, String username, String password, boolean offlineMode){
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.offlineMode = offlineMode;
    }
    public void start() throws IOException {
        // We don't yet know what version to connect with, so we must ask the server...
        MCServerInfo serverInfo = new MCServerInfo(this.host, this.port);
        int protocolVersion = serverInfo.getProtocolVersion();

        if(!offlineMode){
            // We should attempt to authenticate the user
            JsonObject authResponse = new Authentication().mojangAuth(username, password);
            if(authResponse.has("error")){
                throw new IOException(authResponse.toString());
            }
            String playerUsername = authResponse.get("selectedProfile").getAsJsonObject().get("name").getAsString();
            String playerUuid = authResponse.get("selectedProfile").getAsJsonObject().get("id").getAsString();
            String playerToken = authResponse.get("clientToken").getAsString();
            this.player = new Player(playerUsername, playerUuid, playerToken);
        }else{
            this.player = new Player(this.username);
        }

        // Connect, make handshake
        Connection con = new Connection(this.host, this.port);
        con.makeHandshake(ProtocolStates.LOGIN, protocolVersion);

        // Begin login state, start the login process
        LoginStartPacket loginStartPacket = new LoginStartPacket(con.shouldCompress, con.shouldEncrypt, this.player.getUsername());
        con.sendPacket(loginStartPacket);

        // We could now have a few different packets, but the Connection checks what it is
        Packet packet = con.readPacket();

        if(packet.getType() == PacketTypes.ENCRYPTION_REQUEST){
            // The server has requested we enable encryption
            EncryptionRequestPacket encryptionRequestPacket = (EncryptionRequestPacket) packet;
            EncryptionResponsePacket encryptionResponsePacket = new EncryptionResponsePacket(
                    con.shouldCompress,
                    con.shouldEncrypt,
                    encryptionRequestPacket.getPublicKey(),
                    encryptionRequestPacket.getVerifyToken()
            );

            // here we have to authenticate the login with Minecraft
            String serverHash = EncryptionTools.getServerIdHash(
                    encryptionRequestPacket.getServerId(),
                    encryptionRequestPacket.getPublicKey(),
                    encryptionResponsePacket.getSharedSecret()
            );
            JsonObject response = Authentication.authenticateSession(
                    this.player.getToken(),
                    this.player.getUuid().replace("-", ""),
                    serverHash
            );
            if(response != null){
                throw new IOException("Session Authentication failed");
            }
            con.sendPacket(encryptionResponsePacket);
            Packet packetnew = con.readPacket();
        }else {
            // The server wants no encryption, happens if running in offline mode
            // This also means we do not authenticate the user with Mojang/Microsoft
            if(!offlineMode){
                // We expected the server to be running in online mode and haven't authenticated the user!
                throw new IOException("This server is running in online mode, but we expected offline mode");
            }
            if(packet.getType() == PacketTypes.LOGIN_SUCCESS){
                // The server wants no compression, so we will not compress any packets, and move to the PLAY state
                System.out.println("No encryption, no compression");
            }else if(packet.getType() == PacketTypes.SET_COMPRESSION){
                // The server has requested we compress packets from now on
                System.out.println("No encryption, setting compression");
                con.setCompression();
            }else{
                throw new IOException("Unknown packetId");
            }
        }
        // Clean up
        con.close();
    }
}

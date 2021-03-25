package com.watsontv.mcchat.packets.status.clientbound;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.DataInputStream;
import java.io.IOException;

public class ResponsePacket extends Packet {
    /** A packet sent by the server containing a JSON string with the server info */
    JsonObject json;
    public ResponsePacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.RESPONSE;
    }

    @Override
    public Packet readFromInput(DataInputStream inputStream) throws IOException {
        // At this point, we've already read the length and ID of the packet
        int jsonLength = VarInt.readVarInt(inputStream);
        byte[] in = new byte[jsonLength];
        inputStream.readFully(in);
        String jsonStr = new String(in);
        this.json = new Gson().fromJson(jsonStr, JsonObject.class);
        return this;
    }

    public String getServerDescription(){
        return this.json.get("description").getAsJsonObject().get("text").getAsString();
    }

    public JsonObject getPlayerData(){
        return this.json.getAsJsonObject("players");
    }

    public String getVersionName(){
        return this.json.get("version").getAsJsonObject().get("name").getAsString();
    }

    public int getProtocolVersion(){
        return this.json.get("version").getAsJsonObject().get("protocol").getAsInt();
    }
}

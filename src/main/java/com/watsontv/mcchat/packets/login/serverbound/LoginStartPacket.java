package com.watsontv.mcchat.packets.login.serverbound;

import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.IOException;

public class LoginStartPacket extends Packet {
    /** A packet to indicate to the server that we would like to begin the login sequence.
     * The only field is the player username. */
    public LoginStartPacket(boolean compressionEnabled, boolean encryptionEnabled, String username) throws IOException {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.LOGIN_START;
        VarInt.writeVarInt(dataOutputStream, this.packetType);
        VarInt.writeVarInt(dataOutputStream, username.getBytes().length);
        this.dataOutputStream.write(username.getBytes());
    }
}

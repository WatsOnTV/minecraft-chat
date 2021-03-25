package com.watsontv.mcchat.packets.status.serverbound;

import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.IOException;

public class PingPacket extends Packet {
    /** A packet whose only field is a random long number. Notchian uses the current time (in ms), so I have too */
    public PingPacket(boolean compressionEnabled, boolean encryptionEnabled, long payload) throws IOException {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.PING;
        VarInt.writeVarInt(dataOutputStream, this.packetType);
        this.dataOutputStream.writeLong(payload);
    }
}

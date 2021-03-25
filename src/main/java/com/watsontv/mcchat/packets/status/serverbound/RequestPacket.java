package com.watsontv.mcchat.packets.status.serverbound;

import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.IOException;

public class RequestPacket extends Packet {
    /** A packet which has no fields */
    public RequestPacket(boolean compressionEnabled, boolean encryptionEnabled) throws IOException {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.REQUEST;
        VarInt.writeVarInt(this.dataOutputStream, this.packetType);
    }
}

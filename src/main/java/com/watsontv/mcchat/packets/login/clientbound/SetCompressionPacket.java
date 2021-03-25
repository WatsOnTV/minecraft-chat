package com.watsontv.mcchat.packets.login.clientbound;

import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

public class SetCompressionPacket extends Packet {
    public SetCompressionPacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.SET_COMPRESSION;
    }
}

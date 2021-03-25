package com.watsontv.mcchat.packets.status.clientbound;

import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.DataInputStream;
import java.io.IOException;

public class PongPacket extends Packet {
    /** A packet replying to our ping packet */
    long payload;

    public PongPacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.PONG;
    }

    @Override
    public Packet readFromInput(DataInputStream inputStream) throws IOException {
        this.payload = inputStream.readLong();
        return this;
    }

    public long getPayload() {
        return payload;
    }
}

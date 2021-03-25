package com.watsontv.mcchat.packets;

import com.watsontv.mcchat.VarInt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HandshakePacket extends Packet{
    public HandshakePacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
    }

    public ByteArrayOutputStream getHandshake(String host, int port, int nextState, int protocol) throws IOException {
        ByteArrayOutputStream handshakeBytes = new ByteArrayOutputStream();
        DataOutputStream handshakePacket = new DataOutputStream(handshakeBytes);
        handshakePacket.writeByte(PacketTypes.HANDSHAKE);               // Handshake type: 0x00
        VarInt.writeVarInt(handshakePacket, protocol);                  // Protocol Version: -1 for unknown
        VarInt.writeVarInt(handshakePacket, host.length());
        handshakePacket.writeBytes(host);
        handshakePacket.writeShort(port);
        VarInt.writeVarInt(handshakePacket, nextState);
        handshakePacket.close();
        return handshakeBytes;
    }
}

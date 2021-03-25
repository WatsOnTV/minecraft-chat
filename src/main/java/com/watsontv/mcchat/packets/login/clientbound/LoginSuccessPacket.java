package com.watsontv.mcchat.packets.login.clientbound;

import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

public class LoginSuccessPacket extends Packet {
    public LoginSuccessPacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.LOGIN_SUCCESS;
    }
}

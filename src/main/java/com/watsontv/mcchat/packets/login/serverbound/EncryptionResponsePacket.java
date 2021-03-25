package com.watsontv.mcchat.packets.login.serverbound;

import com.watsontv.mcchat.EncryptionTools;
import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.PublicKey;

public class EncryptionResponsePacket extends Packet {
    PublicKey publicKey;
    byte[] verifyToken;
    SecretKey sharedSecret;
    byte[] sharedSecretCiphertext;
    byte[] verifyTokenCiphertext;
    public EncryptionResponsePacket(boolean compressionEnabled, boolean encryptionEnabled, PublicKey publicKey, byte[] verifyToken) throws IOException {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.ENCRYPTION_RESPONSE;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
        System.out.println(publicKey.getAlgorithm());
        this.verifyTokenCiphertext = EncryptionTools.getEncrypted(this.publicKey, this.verifyToken);
        this.sharedSecret = EncryptionTools.getSharedSecret();
        this.sharedSecretCiphertext = EncryptionTools.getEncrypted(this.publicKey, this.sharedSecret.getEncoded());

        System.out.println(new String(verifyTokenCiphertext));

        VarInt.writeVarInt(dataOutputStream, this.packetType);
        VarInt.writeVarInt(dataOutputStream, this.sharedSecretCiphertext.length);
        dataOutputStream.write(this.sharedSecretCiphertext);
        VarInt.writeVarInt(dataOutputStream, this.verifyTokenCiphertext.length);
        dataOutputStream.write(this.verifyTokenCiphertext);
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getSharedSecretCiphertext() {
        return sharedSecretCiphertext;
    }

    public SecretKey getSharedSecret() {
        return sharedSecret;
    }
}

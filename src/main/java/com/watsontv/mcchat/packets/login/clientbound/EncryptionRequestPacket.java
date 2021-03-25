package com.watsontv.mcchat.packets.login.clientbound;

import com.watsontv.mcchat.VarInt;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class EncryptionRequestPacket extends Packet {
    String serverId;
    PublicKey publicKey;
    byte[] verifyToken;
    public EncryptionRequestPacket(boolean compressionEnabled, boolean encryptionEnabled) {
        super(compressionEnabled, encryptionEnabled);
        this.packetType = PacketTypes.ENCRYPTION_REQUEST;
    }

    @Override
    public Packet readFromInput(DataInputStream inputStream) throws IOException {
        super.readFromInput(inputStream);
        // At this point, we've already read the length and ID of the packet
        int stringLen = VarInt.readVarInt(inputStream);
        byte[] serverId = new byte[stringLen];
        if(inputStream.read(serverId, 0, stringLen) == -1){
            throw new IOException("Error reading ServerID");
        }
        this.serverId = new String(serverId, StandardCharsets.UTF_8);
        int pubKeyLen = VarInt.readVarInt(inputStream);
        byte[] pubKey = new byte[pubKeyLen];
        if(inputStream.read(pubKey, 0, pubKeyLen) == -1){
            throw new IOException("Error reading PublicKey");
        }
        int verifyTokenLen = VarInt.readVarInt(inputStream);
        this.verifyToken = new byte[verifyTokenLen];
        if(inputStream.read(verifyToken) == -1){
            throw new IOException("Error reading VerifyToken");
        }

        try{
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKey));
            System.out.println(publicKey);
            System.out.println(new String(verifyToken));
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new IOException("Could not obtain public key", e);
        }
        return this;
    }

    public String getServerId() {
        return serverId;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }
    public byte[] getVerifyToken() {
        return verifyToken;
    }
}

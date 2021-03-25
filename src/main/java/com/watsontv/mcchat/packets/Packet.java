package com.watsontv.mcchat.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet {
    public ByteArrayOutputStream byteArrayOutputStream;
    public DataOutputStream dataOutputStream;
    public int packetType;
    public boolean compressionEnabled;
    public boolean encryptionEnabled;
    public Packet(boolean compressionEnabled, boolean encryptionEnabled){
        this.compressionEnabled = compressionEnabled;
        this.encryptionEnabled = encryptionEnabled;
        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStream(byteArrayOutputStream);
    }

    /** Gets the assembled packet, excluding the length, which is added when the packet is sent */
    public ByteArrayOutputStream get(){
        return byteArrayOutputStream;
    }

    /** Method intended for overwrite, only for Packets which are sent from the server */
    public Packet readFromInput(DataInputStream inputStream) throws IOException {
        return this;
    }

    /** Returns the type of packet as an integer */
    public int getType(){
        return this.packetType;
    }

    /** Closes all open streams. */
    public void close() throws IOException {
        this.byteArrayOutputStream.close();
        this.dataOutputStream.close();
    }
}

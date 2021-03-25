package com.watsontv.mcchat;

import com.watsontv.mcchat.packets.HandshakePacket;
import com.watsontv.mcchat.packets.Packet;
import com.watsontv.mcchat.packets.PacketTypes;
import com.watsontv.mcchat.packets.login.clientbound.EncryptionRequestPacket;
import com.watsontv.mcchat.packets.login.clientbound.LoginSuccessPacket;
import com.watsontv.mcchat.packets.login.clientbound.SetCompressionPacket;
import com.watsontv.mcchat.packets.login.serverbound.EncryptionResponsePacket;
import com.watsontv.mcchat.packets.status.clientbound.PongPacket;
import com.watsontv.mcchat.packets.status.clientbound.ResponsePacket;
import com.watsontv.mcchat.protocol.ProtocolStates;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PublicKey;

public class Connection {
    Socket socket;
    int timeout = 5000;
    InetSocketAddress addr;
    OutputStream outputStream;
    InputStream inputStream;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    boolean connected;
    boolean shouldCompress = false;
    boolean shouldEncrypt = false;
    int currentState;
    PublicKey publicKey;
    SecretKey secretKey;


    Connection(String host, int port) throws IOException {
        this.socket = new Socket();
        this.addr = new InetSocketAddress(host, port);
        this.socket.setSoTimeout(this.timeout);
        this.socket.connect(addr, this.timeout);
        this.connected = true;
        this.outputStream = socket.getOutputStream();
        this.dataOutputStream = new DataOutputStream(outputStream);
        this.inputStream = socket.getInputStream();
        this.dataInputStream = new DataInputStream(inputStream);
        this.currentState = ProtocolStates.STATUS;
    }

    public DataInputStream getInputStream(){
        return this.dataInputStream;
    }

    public DataOutputStream getOutputStream() {
        return dataOutputStream;
    }

    /** Close all connections, if not already closed */
    public void close() throws IOException {
        if(this.connected){
            this.dataOutputStream.close();
            this.dataInputStream.close();
            this.outputStream.close();
            this.inputStream.close();
            this.socket.close();
            this.connected = false;
        }
    }

    public void updateState(int newState){
        this.currentState = newState;
    }

    /** Write a VarInt to the output stream, using VarInt class */
    public void writeVarInt(int varInt) throws IOException {
        VarInt.writeVarInt(this.dataOutputStream, varInt);
    }

    /** Read a VarInt from the input stream and return an int */
    public int readVarInt() throws IOException {
        return VarInt.readVarInt(this.getInputStream());
    }

    /** Writes the necessary bytes to form a HandShake packet */
    public void makeHandshake(int nextState, int protocolVersion) throws IOException {
        this.currentState = ProtocolStates.HANDSHAKING;
        if(connected){
            ByteArrayOutputStream handshakeBytes = new
                    HandshakePacket(this.shouldCompress, this.shouldEncrypt).getHandshake(
                        this.addr.getHostName(), this.addr.getPort(), nextState, protocolVersion
                    );
            this.writeVarInt(handshakeBytes.size());                              // Prepend the size of the handshake
            this.getOutputStream().write(handshakeBytes.toByteArray());           // Write the packet
            handshakeBytes.close();
        }else{
            throw new IOException("Connection to the server has already been closed");
        }
        this.currentState = nextState;
    }

    /** Enables packet compressions */
    public void setCompression(){
        this.shouldCompress = true;
    }

    /** Enables packet encryption */
    public void setEncryption(){
        this.shouldEncrypt = true;
    }

    /** Sends the given packet to the server */
    public void sendPacket(Packet packet) throws IOException {
        if(connected){
            if(this.shouldEncrypt){

            }else{
                ByteArrayOutputStream packetAssembled = packet.get();
                this.getOutputStream().write(packetAssembled.size());
                this.getOutputStream().write(packetAssembled.toByteArray());
                if(packet instanceof EncryptionResponsePacket){
                    this.shouldEncrypt = true;
                    this.publicKey = ((EncryptionResponsePacket) packet).getPublicKey();
                    this.secretKey = ((EncryptionResponsePacket) packet).getSharedSecret();
                }
            }
        }else{
            throw new IOException("Connection to the server has already been closed");
        }
    }

    /** Reads a packet from the server */
    public Packet readPacket() throws IOException{
        if(!connected){
            throw new IOException("Connection to the server has already been closed");
        }
        int packetLen;
        int packetId;
        DataInputStream encryptedStream;
        if(shouldEncrypt){
            // Do something else...
            byte[] decryptedData = EncryptionTools.getDecrypted(secretKey, dataInputStream.readAllBytes());
            encryptedStream = new DataInputStream(new ByteArrayInputStream(decryptedData));
            packetLen = VarInt.readVarInt(encryptedStream);
            System.out.println(decryptedData.length);
            packetId = VarInt.readVarInt(encryptedStream);
        }else{
            encryptedStream = null;
            packetLen = this.readVarInt();
            packetId = this.readVarInt();
        }
        Packet packet;
        DataInputStream toReadFrom = encryptedStream == null ? this.dataInputStream : encryptedStream;
        if(currentState == ProtocolStates.HANDSHAKING){
            // We will never actually reach here, as the server switches states immediately after the handshake
            throw new IllegalStateException("Unknown state ID " + currentState);
        } else if(currentState == ProtocolStates.STATUS){
            System.out.println("Packet in state STATUS");
            packet = switch (packetId) {
                case PacketTypes.RESPONSE -> new ResponsePacket(this.shouldCompress, this.shouldEncrypt);
                case PacketTypes.PONG -> new PongPacket(this.shouldCompress, this.shouldEncrypt);
                default -> throw new IllegalStateException("Unknown packetId: " + packetId + " in state " + currentState);
            };

        } else if(currentState == ProtocolStates.LOGIN){
            System.out.println("Packet in state LOGIN");
            packet = switch (packetId) {
                case PacketTypes.ENCRYPTION_REQUEST -> new EncryptionRequestPacket(this.shouldCompress, this.shouldEncrypt);
                case PacketTypes.SET_COMPRESSION -> new SetCompressionPacket(this.shouldCompress, this.shouldEncrypt);
                case PacketTypes.LOGIN_SUCCESS -> new LoginSuccessPacket(this.shouldCompress, this.shouldEncrypt);

                default -> throw new IllegalStateException("Unknown packetId: " + packetId + " in state " + currentState);
            };

        } else if(currentState == ProtocolStates.PLAY){
            System.out.println("Packet in state PLAY");
            switch (packetId) {
                default -> throw new IllegalStateException("Unknown packetId: " + packetId + " in state " + currentState);
            }

        } else{
            throw new IllegalStateException("Unknown state ID " + currentState);
        }

        return packet.readFromInput(toReadFrom);
    }
}

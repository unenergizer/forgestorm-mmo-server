package com.valenguard.server.network.shared;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.ServerAbstractOutPacket;
import com.valenguard.server.network.packet.out.ValenguardOutputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

@SuppressWarnings("unused")
@Getter
public class ClientHandler {

    private Socket socket;
    private ValenguardOutputStream valenguardOutputStream;
    private DataInputStream inputStream;

    @Setter
    private Player player;

    public ClientHandler(Socket socket, ValenguardOutputStream valenguardOutputStream, DataInputStream inputStream) {
        this.socket = socket;
        this.valenguardOutputStream = valenguardOutputStream;
        this.inputStream = inputStream;
    }

    @FunctionalInterface
    private interface Reader {
        Object accept() throws IOException;
    }

    public boolean readBoolean() {
        return (boolean) readIn(inputStream::readBoolean);
    }

    public String readString() {
        return (String) readIn(inputStream::readUTF);
    }

    public byte readByte() {
        return (byte) readIn(inputStream::readByte);
    }

    public char readChar() {
        return (char) readIn(inputStream::readChar);
    }

    public double readDouble() {
        return (double) readIn(inputStream::readDouble);
    }

    public float readFloat() {
        return (float) readIn(inputStream::readFloat);
    }

    public int readInt() {
        return (int) readIn(inputStream::readInt);
    }

    public long readLong() {
        return (long) readIn(inputStream::readLong);
    }

    public short readShort() {
        return (short) readIn(inputStream::readShort);
    }

    private Object readIn(Reader reader) {
        try {
            return reader.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int fillCurrentBuffer(ServerAbstractOutPacket serverAbstractOutPacket) {
        return valenguardOutputStream.fillCurrentBuffer(serverAbstractOutPacket);
    }

    public void writeBuffers() {
        try {
            valenguardOutputStream.writeBuffers();
        } catch (IOException e) {
            // If the client is not closing their socket.
            if (!(e instanceof SocketException)) e.printStackTrace();
        }
    }

    public void flushBuffer() {
        try {
            valenguardOutputStream.flush();
        } catch (IOException e) {
            // If the client is not closing their socket.
            if (!(e instanceof SocketException)) e.printStackTrace();
        }
    }

    /**
     * Disconnects this client from the server.
     */
    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (valenguardOutputStream != null) valenguardOutputStream.close();
            if (inputStream != null) inputStream.close();
            socket = null;
            valenguardOutputStream = null;
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

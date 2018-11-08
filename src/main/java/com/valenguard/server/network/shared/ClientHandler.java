package com.valenguard.server.network.shared;

import com.valenguard.server.game.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@SuppressWarnings("unused")
@Getter
public class ClientHandler {

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    @Setter
    private Player player;

    public ClientHandler(Socket socket, ObjectOutputStream outputStream, ObjectInputStream inputStream) {
        this.socket = socket;
        this.outputStream = outputStream;
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

    /**
     * This is used to send the entity packet data.
     *
     * @param opcode        The code that defines what this packet contents will contain.
     * @param writeCallback The data we will be sending to the client.
     */
    public void write(byte opcode, Write writeCallback) {
        try {
            outputStream.writeByte(opcode);
            writeCallback.accept(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disconnects this client from the server.
     */
    public void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            socket = null;
            outputStream = null;
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

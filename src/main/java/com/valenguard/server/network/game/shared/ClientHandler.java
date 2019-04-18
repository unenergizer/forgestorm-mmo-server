package com.valenguard.server.network.game.shared;

import com.valenguard.server.game.character.CharacterDataOut;
import com.valenguard.server.game.character.CharacterUtil;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.AbstractServerOutPacket;
import com.valenguard.server.network.game.packet.out.GameOutputStream;
import com.valenguard.server.util.Log;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClientHandler {

    private final int databaseUserId;
    private Socket socket;
    private GameOutputStream gameOutputStream;
    private DataInputStream inputStream;

    @Getter
    private final Map<Byte, Player> loadedPlayers = new HashMap<>();

    @Getter
    private byte currentPlayerId = 0;

    public void loadAllPlayers(List<CharacterDataOut> characterDataOutList) {
        for (byte i = 0; i < characterDataOutList.size(); i++) {
            CharacterDataOut characterDataOut = characterDataOutList.get(i);

            // Skip already loaded player clients / Prevent overwriting or unnecessary adding...
            if (!loadedPlayers.isEmpty()) {
                if (loadedPlayers.containsKey(i)) {
                    if (characterDataOut.getCharacterId() == loadedPlayers.get(i).getCharacterDatabaseId()) continue;
                }
            }

            final Player player = new Player(this, characterDataOut.getCharacterId());

            player.setName(characterDataOut.getName());
            player.setAppearance(CharacterUtil.generateAppearance(
                    player,
                    characterDataOut.getBodyId(),
                    characterDataOut.getHeadId(),
                    characterDataOut.getColorId()));

            loadedPlayers.put(i, player);
        }
    }

    public Player getPlayer() {
        return loadedPlayers.get(currentPlayerId);
    }

    public void setCurrentPlayerId(byte characterId) {
        currentPlayerId = characterId;
    }

    public ClientHandler(final int databaseUserId, final Socket socket, final GameOutputStream gameOutputStream, final DataInputStream inputStream) {
        this.databaseUserId = databaseUserId;
        this.socket = socket;
        this.gameOutputStream = gameOutputStream;
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
        return (String) readIn(() -> {
            byte stringLength;
            // The byte the client sent can indeed be negative.
            try {
                stringLength = inputStream.readByte();
            } catch (EOFException e) {
                Log.println(getClass(), "The client tried sending a string length of negative.", true);
                return ""; // The client sent a negative value for some reason.
            }
            if (((stringLength >>> 8) & 0x01) != 0) {
                // we might later want to use two's compliment method
                Log.println(getClass(), "The client tried sending a string length of negative.", true);
                return "";
            }
            byte[] charArray = new byte[stringLength];
            inputStream.read(charArray);
            StringBuilder stringBuilder = new StringBuilder();
            for (byte ch : charArray) {
                stringBuilder.append((char) ch);
            }
            return stringBuilder.toString();
        });
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

    public int fillCurrentBuffer(AbstractServerOutPacket abstractServerOutPacket) {
        return gameOutputStream.fillCurrentBuffer(abstractServerOutPacket);
    }

    public void writeBuffers() {
        try {
            gameOutputStream.writeBuffers();
        } catch (IOException e) {
            // If the client is not closing their socket.
            if (!(e instanceof SocketException)) e.printStackTrace();
        }
    }

    public void flushBuffer() {
        try {
            gameOutputStream.flush();
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
            if (socket != null && !socket.isClosed()) socket.close();
            if (gameOutputStream != null) gameOutputStream.close();
            if (inputStream != null) inputStream.close();
            socket = null;
            gameOutputStream = null;
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

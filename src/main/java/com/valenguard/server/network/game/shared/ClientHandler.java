package com.valenguard.server.network.game.shared;

import com.valenguard.server.database.AuthenticatedUser;
import com.valenguard.server.database.sql.GamePlayerInventorySQL;
import com.valenguard.server.game.character.CharacterDataOut;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.WearableItemStack;
import com.valenguard.server.game.world.item.inventory.EquipmentSlotTypes;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
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

@SuppressWarnings("unused")
@Getter
public class ClientHandler {

    private final AuthenticatedUser authenticatedUser;
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

            Player player;

            // Skip already loaded player clients / Prevent overwriting or unnecessary adding...
            if (!loadedPlayers.isEmpty()) {
                if (loadedPlayers.containsKey(i) && characterDataOut.getCharacterId() == loadedPlayers.get(i).getDatabaseId()) {
                    // Use existing player
                    player = loadedPlayers.get(i);
                } else {
                    // Player doesn't exist, create a new one
                    player = new Player(this);
                }
            } else {
                // Player doesn't exist, create a new one
                player = new Player(this);
            }

            player.setName(characterDataOut.getName());
            player.setDatabaseId(characterDataOut.getCharacterId());

            // Here we access the players inventory via SQL. Load it in, and get the items the player is wearing.
            // We could do well here by optimizing this. This inventory is loaded twice.
            InventorySlot[] playerEquipment = new GamePlayerInventorySQL().databaseLoadAppearance(player);

            Appearance appearance = new Appearance(player);
            player.setAppearance(appearance);

            appearance.setHairTexture(characterDataOut.getHeadTexture());
            appearance.setHelmTexture(getTextureID(playerEquipment, EquipmentSlotTypes.HELM));
            appearance.setChestTexture(getTextureID(playerEquipment, EquipmentSlotTypes.CHEST));
            appearance.setPantsTexture(getTextureID(playerEquipment, EquipmentSlotTypes.PANTS));
            appearance.setShoesTexture(getTextureID(playerEquipment, EquipmentSlotTypes.BOOTS));
            appearance.setHairTexture(characterDataOut.getHeadTexture());
            appearance.setHairColor(characterDataOut.getHairColor());
            appearance.setEyeColor(characterDataOut.getEyeColor());
            appearance.setSkinColor(characterDataOut.getSkinColor());
            // TODO : Send glove data/color?
            appearance.setLeftHandTexture(getTextureID(playerEquipment, EquipmentSlotTypes.WEAPON));
            appearance.setRightHandTexture(getTextureID(playerEquipment, EquipmentSlotTypes.SHIELD));

            loadedPlayers.put(i, player);
        }
    }

    private byte getTextureID(InventorySlot[] playerEquipment, EquipmentSlotTypes equipmentSlotTypes) {
        if (playerEquipment.length == 0) {
            // This is a hacky fix. When a new character is created, they will not yet
            // have an Equipment inventory to load from. Thus the size of the array is 0.
            // This prevents an ArrayOutOfBoundsException.
            return -1;
        }
        ItemStack itemStack = playerEquipment[equipmentSlotTypes.getSlotIndex()].getItemStack();
        if (itemStack == null) {
            return -1;
        } else {
            return ((WearableItemStack) itemStack).getTextureId();
        }
    }

    public Player getPlayer() {
        Player player = loadedPlayers.get(currentPlayerId);

        if (player == null) {
            player = new Player(this);
        }

        return player;
    }

    public void setCurrentPlayerId(byte characterId) {
        currentPlayerId = characterId;
    }

    public ClientHandler(final AuthenticatedUser authenticatedUser, final Socket socket, final GameOutputStream gameOutputStream, final DataInputStream inputStream) {
        this.authenticatedUser = authenticatedUser;
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
            //noinspection ResultOfMethodCallIgnored
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

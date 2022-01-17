package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.maps.Floors;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.WorldChunk;
import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.maps.tile.TileImage;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOutOut;
import com.forgestorm.server.network.game.packet.out.WorldBuilderPacketOutOut;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.WORLD_BUILDER)
public class WorldBuilderPacketIn implements PacketListener<WorldBuilderPacketIn.WorldBuilderPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        LayerDefinition layerDefinition = LayerDefinition.getLayerDefinition(clientHandler.readByte());
        int textureId = clientHandler.readInt();
        int tileX = clientHandler.readInt();
        int tileY = clientHandler.readInt();
        short worldZ = clientHandler.readShort();

        println(getClass(), "LayerDefinition: " + layerDefinition, false, PRINT_DEBUG);
        println(getClass(), "textureId: " + textureId, false, PRINT_DEBUG);
        println(getClass(), "tileX: " + tileX, false, PRINT_DEBUG);
        println(getClass(), "tileY: " + tileY, false, PRINT_DEBUG);
        println(getClass(), "worldZ: " + worldZ, false, PRINT_DEBUG);

        return new WorldBuilderPacket(clientHandler.getPlayer(), layerDefinition, textureId, tileX, tileY, worldZ);
    }

    @Override
    public boolean sanitizePacket(WorldBuilderPacket packetData) {
        // TODO: Make sure this person can build...
        return true;
    }

    @Override
    public void onEvent(WorldBuilderPacket packetData) {
        // Set the tile in the world
        GameWorld gameWorld = packetData.playerSender.getGameWorld();
        WorldChunk worldChunk = gameWorld.findChunk(packetData.tileX, packetData.tileY);

        if (worldChunk == null) {
            new ChatMessagePacketOutOut(
                    packetData.playerSender,
                    ChatChannelType.GENERAL,
                    MessageText.ERROR + "You cannot place a tile here. Chunk does not exist.").sendPacket();
            return;
        }

        TileImage tileImage = ServerMain.getInstance().getWorldBuilder().getTileImageMap().get(packetData.textureId);
        int localX = packetData.tileX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE;
        int localY = packetData.tileY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE;

        worldChunk.setTileImage(packetData.layerDefinition, tileImage, localX, localY, Floors.getFloor(packetData.worldZ));

        // For now, just resend the building packet to all (but original playerSender)
        ServerMain.getInstance().getGameManager().sendToAllButPlayer(packetData.playerSender, clientHandler ->
                new WorldBuilderPacketOutOut(clientHandler, packetData.layerDefinition, packetData.textureId, packetData.tileX, packetData.tileY, packetData.worldZ).sendPacket());
    }

    @AllArgsConstructor
    static class WorldBuilderPacket extends PacketData {
        private final Player playerSender;
        private final LayerDefinition layerDefinition;
        private final int textureId;
        private final int tileX;
        private final int tileY;
        private final short worldZ;
    }
}
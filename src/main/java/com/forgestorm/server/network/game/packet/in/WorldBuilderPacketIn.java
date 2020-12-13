package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.WorldChunk;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.network.game.packet.out.WorldBuilderPacketOut;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.WORLD_BUILDER)
public class WorldBuilderPacketIn implements PacketListener<WorldBuilderPacketIn.WorldBuilderPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        LayerDefinition layerDefinition = LayerDefinition.getLayerDefinition(clientHandler.readByte());
        int textureId = clientHandler.readInt();
        short tileX = clientHandler.readShort();
        short tileY = clientHandler.readShort();
        return new WorldBuilderPacket(clientHandler.getPlayer(), layerDefinition, textureId, tileX, tileY);
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
            new ChatMessagePacketOut(
                    packetData.playerSender,
                    ChatChannelType.GENERAL,
                    MessageText.ERROR + "You cannot place a tile here. Chunk does not exist.").sendPacket();
            return;
        }

        TileImage tileImage = ServerMain.getInstance().getWorldBuilder().getTileImageMap().get(packetData.textureId);
        int localX = packetData.tileX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE;
        int localY = packetData.tileY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE;

        worldChunk.setTileImage(packetData.layerDefinition, tileImage, localX, localY);

        // For now, just resend the building packet to all (but original playerSender)
        ServerMain.getInstance().getGameManager().sendToAllButPlayer(packetData.playerSender, clientHandler ->
                new WorldBuilderPacketOut(clientHandler, packetData.layerDefinition, packetData.textureId, packetData.tileX, packetData.tileY).sendPacket());
    }

    @AllArgsConstructor
    class WorldBuilderPacket extends PacketData {
        private Player playerSender;
        private LayerDefinition layerDefinition;
        private int textureId;
        private short tileX;
        private short tileY;
    }
}
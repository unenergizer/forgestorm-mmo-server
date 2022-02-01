package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.maps.Floors;
import com.forgestorm.server.game.world.maps.tile.Tile;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class WorldChunkPartPacketOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final short chunkX, chunkY;
    private final Floors floor;
    private final byte layerDefinitionByte;
    private final byte sectionsSent;
    private final Tile[] arraySend;

    public WorldChunkPartPacketOut(Player player, short chunkX, short chunkY, Floors floor, byte layerDefinitionByte, byte sectionsSent, Tile[] arraySend) {
        super(Opcodes.WORLD_CHUNK_LAYER, player.getClientHandler());
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.floor = floor;
        this.layerDefinitionByte = layerDefinitionByte;
        this.sectionsSent = sectionsSent;
        this.arraySend = arraySend;
        println(getClass(), "Sending chunk section! Layer: " + layerDefinitionByte + ", Section: " + sectionsSent, true, PRINT_DEBUG);
    }

    @Override
    public void createPacket(GameOutputStream write) {
        // Write Chunk location
        write.writeShort(chunkX);
        write.writeShort(chunkY);

        // Write layer information
        write.writeShort(floor.getWorldZ());
        write.writeByte(layerDefinitionByte);
        write.writeByte(sectionsSent);

        // Write all tile images.
        for (Tile tile : arraySend) {
            if (tile.getTileImage() == null) {
                write.writeInt(0); // 0 represents a null TileImage
            } else {
                write.writeInt(tile.getTileImage().getImageId());
            }
        }
    }
}

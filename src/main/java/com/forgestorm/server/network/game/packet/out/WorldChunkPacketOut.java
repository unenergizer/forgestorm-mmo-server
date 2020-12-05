package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.WorldChunk;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.network.game.shared.Opcodes;

import java.util.Map;

public class WorldChunkPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final WorldChunk worldChunk;
    private byte section;

    public WorldChunkPacketOut(final Player player, WorldChunk worldChunk) {
        super(Opcodes.WORLD_CHUNK, player.getClientHandler());
        this.worldChunk = worldChunk;
    }

    public WorldChunkPacketOut(final Player player, WorldChunk worldChunk, byte section) {
        super(Opcodes.WORLD_CHUNK, player.getClientHandler());
        this.worldChunk = worldChunk;
        this.section = section;
    }

    @Override
    protected void createPacket(GameOutputStream write) {

        write.writeByte(section);

        TileImage[] images = worldChunk.getLayers().get(0);
        int offset = section * 4 * 4;
        for (int i = offset; i < offset + 4*4; i++) {
            write.writeInt(images[i].getImageId());
        }


        // Write chunk location
        write.writeShort(worldChunk.getChunkX());
        write.writeShort(worldChunk.getChunkY());

        // Write number of layers the chunk has
        write.writeShort((byte) worldChunk.getLayers().size());

        // Describe chunk contents
        for (Map.Entry<LayerDefinition, TileImage[]> entry : worldChunk.getLayers().entrySet()) {
            LayerDefinition layerDefinition = entry.getKey();
            TileImage[] tileImages = entry.getValue();

            // Write layerDefinition
            write.writeByte(layerDefinition.getLayerDefinitionByte());

            // Write all tile images.
            for (TileImage tileImage : tileImages) {
                if (tileImage == null) {
                    write.writeInt(0); // 0 represents a null TileImage
                } else {
                    write.writeInt(tileImage.getImageId());
                }
            }
        }
    }
}

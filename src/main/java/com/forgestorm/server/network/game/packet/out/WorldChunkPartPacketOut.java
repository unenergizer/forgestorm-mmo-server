package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class WorldChunkPartPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = true;

    private final short chunkX, chunkY;
    private final byte layerDefinitionByte;
    private final byte sectionsSent;
    private final TileImage[] arraySend;

    public WorldChunkPartPacketOut(Player player, short chunkX, short chunkY, byte layerDefinitionByte, byte sectionsSent, TileImage[] arraySend) {
        super(Opcodes.WORLD_CHUNK, player.getClientHandler());
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.layerDefinitionByte = layerDefinitionByte;
        this.sectionsSent = sectionsSent;
        this.arraySend = arraySend;
        println(getClass(), "Sending chunk section! Layer: " + layerDefinitionByte + ", Section: " + sectionsSent, true, PRINT_DEBUG);
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        // Write Chunk location
        write.writeShort(chunkX);
        write.writeShort(chunkY);

        // Write layer information
        write.writeByte(layerDefinitionByte);
        write.writeByte(sectionsSent);

        // Write all tile images.
        for (TileImage tileImage : arraySend) {
            if (tileImage == null) {
                write.writeInt(0); // 0 represents a null TileImage
            } else {
                write.writeInt(tileImage.getImageId());
            }
        }
    }
}
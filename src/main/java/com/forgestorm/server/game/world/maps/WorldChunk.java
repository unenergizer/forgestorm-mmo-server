package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import com.forgestorm.server.network.game.packet.out.WorldChunkPartPacketOut;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldChunk {

    @Getter
    private final short chunkX, chunkY;

    @Setter
    @Getter
    private Map<LayerDefinition, TileImage[]> layers;

    private final Map<Integer, Warp> tileWarps = new HashMap<Integer, Warp>();

    @Getter
    private List<Entity> entities = new ArrayList<>();

    public WorldChunk(short chunkX, short chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    public void setTileImage(LayerDefinition layerDefinition, TileImage tileImage, int localX, int localY) {
        layers.get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE] = tileImage;
    }

    TileImage getTileImage(LayerDefinition layerDefinition, int localX, int localY) {
        return layers.get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE];
    }

    public boolean isTraversable(int localX, int localY) {
        for (TileImage[] tileImages : layers.values()) {
            TileImage tileImage = tileImages[localX + localY * GameConstants.CHUNK_SIZE];
            if (tileImage == null) continue;
            if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) return false;
        }
        return true;
    }

    public void addTileWarp(short localX, short localY, Warp warp) {
        tileWarps.put((localX << 16) | (localY & 0xFFFF), warp);
    }

    Warp getWarp(short localX, short localY) {
        if (tileWarps.containsKey((localX << 16) | (localY & 0xFFFF))) {
            return tileWarps.get((localX << 16) | (localY & 0xFFFF));
        }
        return null;
    }

    public void sendChunk(Player chunkRecipient) {
        // First... we don't send chunks. We send layer "parts"
        // The packet must remain under 200 bytes

        for (Map.Entry<LayerDefinition, TileImage[]> layerMap : layers.entrySet()) {
            LayerDefinition layerDefinition = layerMap.getKey();
            TileImage[] tileImages = layerMap.getValue();

            // Construct and send a section
            byte layerSectionsSent = 0;
            for (int t = 0; t < tileImages.length / GameConstants.MAX_TILE_SEND; t++) {
                TileImage[] arraySend = new TileImage[GameConstants.MAX_TILE_SEND];

                // Get the tiles to send
                int j = 0;
                for (int i = layerSectionsSent * GameConstants.MAX_TILE_SEND; i < (layerSectionsSent + 1) * GameConstants.MAX_TILE_SEND; i++) {
                    arraySend[j] = tileImages[i];
                    j++;
                }

                // Construct packet
                new WorldChunkPartPacketOut(
                        chunkRecipient,
                        chunkX,
                        chunkY,
                        layerDefinition.getLayerDefinitionByte(),
                        layerSectionsSent,
                        arraySend
                ).sendPacket();

                layerSectionsSent++;
            }
        }
    }
}

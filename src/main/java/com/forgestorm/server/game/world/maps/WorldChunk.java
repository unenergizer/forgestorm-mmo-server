package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import com.forgestorm.server.network.game.packet.out.TileWarpPacketOut;
import com.forgestorm.server.network.game.packet.out.WorldChunkPartPacketOut;
import com.forgestorm.server.util.RandomUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldChunk {

    @Getter
    private final short chunkX, chunkY;

    @Getter
    private final Map<LayerDefinition, TileImage[]> layers = new HashMap<>();

    private final Map<WarpLocation, Warp> tileWarps = new HashMap<>();

    @Getter
    private final List<Entity> entities = new ArrayList<>();

    public WorldChunk(short chunkX, short chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    public WorldChunk(short chunkX, short chunkY, boolean generateLandscape) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        if (generateLandscape) generateLandscape();
    }

    public void setTileImage(LayerDefinition layerDefinition, TileImage tileImage, int localX, int localY) {
        initTileLayer(layerDefinition);
        layers.get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE] = tileImage;
    }

    public void setTileImage(LayerDefinition layerDefinition, TileImage tileImage, int index) {
        initTileLayer(layerDefinition);
        layers.get(layerDefinition)[index] = tileImage;
    }

    private void initTileLayer(LayerDefinition layerDefinition) {
        if (layers.containsKey(layerDefinition)) return;
        layers.put(layerDefinition, new TileImage[GameConstants.CHUNK_SIZE * GameConstants.CHUNK_SIZE]);
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
        tileWarps.put(new WarpLocation(localX, localY), warp);
    }

    Warp getWarp(short localX, short localY) {
        for (Map.Entry<WarpLocation, Warp> entry : tileWarps.entrySet()) {
            WarpLocation warpLocation = entry.getKey();
            if (warpLocation.getFromX() == localX && warpLocation.getFromY() == localY) return entry.getValue();
        }
        return null;
    }

    private void generateLandscape() {
        for (int i = 0; i < GameConstants.CHUNK_SIZE; i++) {
            for (int j = 0; j < GameConstants.CHUNK_SIZE; j++) {

                int rand = RandomUtil.getNewRandom(0, 100);

                if (rand <= 50) {
                    TileImage tileImage = ServerMain.getInstance().getWorldBuilder().getTileImageMap().get(262);
                    setTileImage(LayerDefinition.GROUND_DECORATION, tileImage, i, j);
                }
            }
        }
    }

    public void sendChunk(Player chunkRecipient) {
        // First... we don't send chunks. We send layer "parts"
        // The packet must remain under 200 bytes

        // Send chunk layers
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

        // Send chunk warps
        boolean clearWarps = true;
        for (Map.Entry<WarpLocation, Warp> entry : tileWarps.entrySet()) {
            WarpLocation warpLocation = entry.getKey();
            Warp warp = entry.getValue();

            new TileWarpPacketOut(chunkRecipient,
                    clearWarps,
                    warpLocation,
                    warp.getWarpDestination().getWorldName(),
                    warp.getWarpDestination().getX(),
                    warp.getWarpDestination().getY(),
                    warp.getDirectionToFace()).sendPacket();

            // Reset only once per chunk send
            clearWarps = false;
        }
    }

    @Override
    public String toString() {
        return "[WorldChunk] ChunkX: " + chunkX + ", ChunkY: " + chunkY;
    }
}

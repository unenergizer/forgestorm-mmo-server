package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.tile.Tile;
import com.forgestorm.server.game.world.maps.tile.TileImage;
import com.forgestorm.server.network.game.packet.out.TileWarpPacketOutOut;
import com.forgestorm.server.network.game.packet.out.WorldChunkPartPacketOutOut;
import com.forgestorm.shared.game.world.maps.Floors;
import com.forgestorm.shared.game.world.maps.Warp;
import com.forgestorm.shared.game.world.maps.WarpLocation;
import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.shared.util.RandomNumberUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.forgestorm.server.util.Log.println;

public class WorldChunk {

    private static final boolean PRINT_DEBUG = false;

    @Getter
    private final String worldName;

    @Getter
    private final short chunkX, chunkY;

    @Getter
    private final Map<Floors, Map<LayerDefinition, Tile[]>> floorLayers = new HashMap<>();

    @Getter
    private final Map<WarpLocation, Warp> tileWarps = new HashMap<>();

    @Getter
    private final List<Entity> entities = new ArrayList<>();

    @Getter
    @Setter
    private boolean changedSinceLastSave = false;

    public WorldChunk(String worldName, short chunkX, short chunkY) {
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        initTileLayers();
    }

    public WorldChunk(String worldName, short chunkX, short chunkY, boolean generateLandscape) {
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        initTileLayers();
        if (generateLandscape) generateLandscape();
    }

    private void initTileLayers() {
        for (Floors floor : Floors.values()) {
            Map<LayerDefinition, Tile[]> layers = new HashMap<>();

            for (LayerDefinition layerDefinition : LayerDefinition.values()) {

                Tile[] tiles = new Tile[GameConstants.CHUNK_SIZE * GameConstants.CHUNK_SIZE];

                // Initialize all tiles
                for (int localX = 0; localX < GameConstants.CHUNK_SIZE; localX++) {
                    for (int localY = 0; localY < GameConstants.CHUNK_SIZE; localY++) {

                        tiles[localX + localY * GameConstants.CHUNK_SIZE] = new Tile(layerDefinition,
                                worldName,
                                this,
                                localX + chunkX * GameConstants.CHUNK_SIZE,
                                localY + chunkY * GameConstants.CHUNK_SIZE,
                                floor.getWorldZ());
                    }
                }

                layers.put(layerDefinition, tiles);
            }

            floorLayers.put(floor, layers);
        }
    }

    public void setChunkFromDisk(WorldChunk chunkFromDisk) {
        // Copy layers and floors
        for (Floors floor : Floors.values()) {
            for (Map.Entry<LayerDefinition, Tile[]> entry : chunkFromDisk.floorLayers.get(floor).entrySet()) {
                LayerDefinition layerDefinition = entry.getKey();
                Tile[] tiles = entry.getValue();

                for (Tile tileFromDisk : tiles) {
                    if (tileFromDisk.getTileImage() == null) continue;
                    int localTileX = tileFromDisk.getWorldX() - GameConstants.CHUNK_SIZE * chunkX;
                    int localTileY = tileFromDisk.getWorldY() - GameConstants.CHUNK_SIZE * chunkY;
                    Tile localTile = getTile(layerDefinition, localTileX, localTileY, floor);
                    localTile.setTileImage(tileFromDisk.getTileImage());
                }
            }
        }

        // Copy Warps
        for (Map.Entry<WarpLocation, Warp> entry : chunkFromDisk.getTileWarps().entrySet()) {
            WarpLocation warpLocation = entry.getKey();
            Warp warp = entry.getValue();

            addTileWarp(warpLocation, warp);
        }
    }

    public void setTileImage(LayerDefinition layerDefinition, TileImage tileImage, int localX, int localY, Floors floor) {
        floorLayers.get(floor).get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE].setTileImage(tileImage);
        changedSinceLastSave = true;
    }

    public Tile getTile(LayerDefinition layerDefinition, int localX, int localY, Floors floor) {
        return floorLayers.get(floor).get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE];
    }

    public boolean isTraversable(int localX, int localY, short worldZ) {
        return isTraversable(Floors.getFloor(worldZ), localX, localY);
    }

    private boolean isTraversable(Floors floor, int localX, int localY) {
        Tile[] tiles = floorLayers.get(floor).get(LayerDefinition.WORLD_OBJECTS);
        Tile tile = tiles[localX + localY * GameConstants.CHUNK_SIZE];
        if (tile == null) return true;
        if (!ServerMain.getInstance().getDoorManager().isDoorwayTraversable(tile)) return false;
        return !tile.hasCollision();
    }

    public void removeTileWarp(int localX, int localY, short localZ) {
        for (Iterator<Map.Entry<WarpLocation, Warp>> iterator = tileWarps.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<WarpLocation, Warp> entry = iterator.next();
            WarpLocation warpLocation = entry.getKey();

            if (warpLocation.getFromX() == localX
                    && warpLocation.getFromY() == localY
                    && warpLocation.getFromZ() == localZ) {
                iterator.remove();
                return;
            }
        }
    }

    public void addTileWarp(int localX, int localY, short localZ, Warp warp) {
        addTileWarp(new WarpLocation(localX, localY, localZ), warp);
    }

    public void addTileWarp(WarpLocation warpLocation, Warp warp) {
        tileWarps.put(warpLocation, warp);
    }

    Warp getWarp(int localX, int localY, short localZ) {
        for (Map.Entry<WarpLocation, Warp> entry : tileWarps.entrySet()) {
            WarpLocation warpLocation = entry.getKey();
            if (warpLocation.getFromX() == localX
                    && warpLocation.getFromY() == localY
                    && warpLocation.getFromZ() == localZ)
                return entry.getValue();
        }
        return null;
    }

    private void generateLandscape() {
        for (int i = 0; i < GameConstants.CHUNK_SIZE; i++) {
            for (int j = 0; j < GameConstants.CHUNK_SIZE; j++) {

                int rand = RandomNumberUtil.getNewRandom(0, 100);

                if (rand <= 20) {
                    TileImage tileImage = ServerMain.getInstance().getWorldBuilder().getTileImageMap().get(262);
                    setTileImage(LayerDefinition.WORLD_OBJECTS, tileImage, i, j, Floors.GROUND_FLOOR);
                }
            }
        }
    }

    public void sendChunk(Player chunkRecipient) {
        // First... we don't send chunks. We send layer "parts"
        // The packet must remain under 200 bytes

        // Send chunk layers
        for (Floors floor : Floors.values()) {
            for (Map.Entry<LayerDefinition, Tile[]> layerMap : floorLayers.get(floor).entrySet()) {
                LayerDefinition layerDefinition = layerMap.getKey();
                Tile[] tileImages = layerMap.getValue();

                // Construct and send a section
                byte layerSectionsSent = 0;
                for (int t = 0; t < tileImages.length / GameConstants.MAX_TILE_SEND; t++) {
                    Tile[] arraySend = new Tile[GameConstants.MAX_TILE_SEND];

                    // Get the tiles to send
                    int j = 0;
                    for (int i = layerSectionsSent * GameConstants.MAX_TILE_SEND; i < (layerSectionsSent + 1) * GameConstants.MAX_TILE_SEND; i++) {
                        arraySend[j] = tileImages[i];
                        j++;
                    }

                    // Construct packet
                    new WorldChunkPartPacketOutOut(
                            chunkRecipient,
                            chunkX,
                            chunkY,
                            floor,
                            layerDefinition.getLayerDefinitionByte(),
                            layerSectionsSent,
                            arraySend
                    ).sendPacket();

                    layerSectionsSent++;
                }
                println(getClass(), "Layer: " + layerDefinition.getLayerName() + ", Sections Sent: " + layerSectionsSent, false, PRINT_DEBUG);
            }
        }

        // Send chunk warps
        boolean clearWarps = true;
        for (Warp warp : tileWarps.values()) {
            new TileWarpPacketOutOut(chunkRecipient,
                    clearWarps,
                    warp.getFromX(),
                    warp.getFromY(),
                    warp.getFromZ(),
                    warp.getWarpDestination().getWorldName(),
                    warp.getWarpDestination().getX(),
                    warp.getWarpDestination().getY(),
                    warp.getWarpDestination().getZ(),
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

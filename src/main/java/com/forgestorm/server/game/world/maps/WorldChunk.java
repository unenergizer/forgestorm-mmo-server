package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;
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

    void setTileImage(LayerDefinition layerDefinition, TileImage tileImage, int localX, int localY) {
        layers.get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE] = tileImage;
    }

    TileImage getTileImage(LayerDefinition layerDefinition, int localX, int localY) {
        return layers.get(layerDefinition)[localX + localY * GameConstants.CHUNK_SIZE];
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
}

package com.forgestorm.server.game.world.maps.building;

import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.io.TilePropertiesLoader;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
public class WorldBuilder implements ManagerStart {

    private Map<Integer, TileImage> tileImageMap;

    @Setter
    private LayerDefinition currentLayer = LayerDefinition.ROOF;
    @Setter
    private int currentTextureId = 0;

    @Override
    public void start() {
        // Load AbstractTileProperty.yaml
        TilePropertiesLoader tilePropertiesLoader = new TilePropertiesLoader();
        tileImageMap = tilePropertiesLoader.loadTileProperties();
    }
}

package com.forgestorm.server.game.world.maps.building;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.game.world.tile.TileImage;
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
        ServerMain.getInstance().getFileManager().loadTilePropertiesData();
        tileImageMap = ServerMain.getInstance().getFileManager().getTilePropertiesData().getWorldImageMap();
    }
}

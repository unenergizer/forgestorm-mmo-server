package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.TileImage;

public interface WorldEdit {

    void applyPropertyToWorld(TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY);

    void removePropertyFromWorld(TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY);
}

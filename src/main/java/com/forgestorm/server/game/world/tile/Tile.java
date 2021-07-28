package com.forgestorm.server.game.world.tile;

import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.properties.CollisionBlockProperty;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final transient List<TileImage> collisionParents = new ArrayList<>(0);
    private final LayerDefinition layerDefinition;

    @Getter
    private final String worldName;

    @Getter
    private final int worldX, worldY;

    @Getter
    private TileImage tileImage;

    public Tile(LayerDefinition layerDefinition, String worldName, int worldX, int worldY) {
        this.layerDefinition = layerDefinition;
        this.worldName = worldName;
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public void setTileImage(TileImage tileImage) {
        if (this.tileImage != null) removeTileImage();

        this.tileImage = tileImage;
        applyTileProperties();
    }

    public void removeTileImage() {
        removeTileProperties();
        tileImage = null;
    }

    private void applyTileProperties() {
        if (tileImage == null) return;

        // DO COLLISION APPLICATION
        if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) {
            CollisionBlockProperty collisionBlockProperty = (CollisionBlockProperty) tileImage.getProperty(TilePropertyTypes.COLLISION_BLOCK);
            collisionBlockProperty.applyPropertyToWorld(tileImage, layerDefinition, worldName, worldX, worldY);
        }
    }

    private void removeTileProperties() {
        if (tileImage == null) return;

        // DO COLLISION REMOVAL
        if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) {
            CollisionBlockProperty collisionBlockProperty = (CollisionBlockProperty) tileImage.getProperty(TilePropertyTypes.COLLISION_BLOCK);
            collisionBlockProperty.removePropertyToWorld(tileImage, layerDefinition, worldName, worldX, worldY);
        }
    }

    public void addCollision(TileImage parent) {
        collisionParents.add(parent);
    }

    public void removeCollision(TileImage parent) {
        collisionParents.remove(parent);
    }

    public boolean hasCollision() {
        return !collisionParents.isEmpty();
    }
}
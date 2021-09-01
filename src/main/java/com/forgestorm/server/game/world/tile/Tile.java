package com.forgestorm.server.game.world.tile;

import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.properties.CollisionBlockProperty;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Tile {

    private final transient List<Integer> collisionParents = new ArrayList<>(0);
    private final LayerDefinition layerDefinition;

    @Getter
    private final String worldName;

    @Getter
    private final int worldX, worldY;

    @Getter
    private final short worldZ;

    @Getter
    private TileImage tileImage;

    public Tile(LayerDefinition layerDefinition, String worldName, int worldX, int worldY, short worldZ) {
        this.layerDefinition = layerDefinition;
        this.worldName = worldName;
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldZ = worldZ;
    }

    public void setTileImage(TileImage tileImage) {
        removeTileImage();

        this.tileImage = tileImage;
        applyTileProperties();
    }

    private void removeTileImage() {
        removeTileProperties();
        tileImage = null;
    }

    private void applyTileProperties() {
        if (tileImage == null) return;

        // DO COLLISION APPLICATION
        if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) {
            CollisionBlockProperty collisionBlockProperty = (CollisionBlockProperty) tileImage.getProperty(TilePropertyTypes.COLLISION_BLOCK);
            collisionBlockProperty.applyPropertyToWorld(tileImage, layerDefinition, worldName, worldX, worldY, worldZ);
        }
    }

    private void removeTileProperties() {
        if (tileImage == null) return;

        // DO COLLISION REMOVAL
        if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) {
            CollisionBlockProperty collisionBlockProperty = (CollisionBlockProperty) tileImage.getProperty(TilePropertyTypes.COLLISION_BLOCK);
            collisionBlockProperty.removePropertyFromWorld(tileImage, layerDefinition, worldName, worldX, worldY, worldZ);
        }
    }

    public void addCollision(TileImage parent) {
        if (collisionParents.contains(parent.getImageId())) return;
        collisionParents.add(parent.getImageId());
    }

    public boolean removeCollision(TileImage parent) {
        // Do not remove cast to Integer
        return collisionParents.remove((Integer) parent.getImageId());
    }

    public boolean hasCollision() {
        return !collisionParents.isEmpty();
    }
}

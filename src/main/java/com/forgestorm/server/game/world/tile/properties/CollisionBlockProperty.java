package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.game.world.tile.TileImage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class CollisionBlockProperty extends AbstractTileProperty implements WorldEdit {

    private static final boolean PRINT_DEBUG = false;

    @Getter
    @Setter
    private List<Boolean> collisionList;

    public CollisionBlockProperty() {
        super(TilePropertyTypes.COLLISION_BLOCK);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {
        println(getClass(), "Tile not traversable.", false, printDebugMessages);

        @SuppressWarnings("unchecked")
        List<Boolean> collisionList = (List<Boolean>) tileProperties.get("collisionList");
        if (collisionList != null) {
            println(getClass(), "Collision Value(s): " + collisionList, false, printDebugMessages);
            setCollisionList(collisionList);
        }

        return this;
    }

    @Override
    public void applyPropertyToWorld(TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY) {
        processCollisionTiles(tileImage, layerDefinition, worldName, worldX, worldY, false);
    }

    @Override
    public void removePropertyFromWorld(TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY) {
        processCollisionTiles(tileImage, layerDefinition, worldName, worldX, worldY, true);
    }

    private void processCollisionTiles(TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY, boolean useEraser) {
        int tilesWide = tileImage.getWidth() / GameConstants.TILE_SIZE;
        int tilesTall = tileImage.getHeight() / GameConstants.TILE_SIZE;

        for (int row = 0; row < tilesTall; row++) {
            for (int column = 0; column < tilesWide; column++) {
                int index = row + column * tilesTall;

                if (collisionList.get(index)) {
                    // Convert the coordinates of the collision areas to world coordinates.
                    // Then add this TileImage as a collision parent in the given
                    // TileImage from the world coordinates found above.

                    int tileX = worldX + column;
                    int tileY = worldY + tilesTall - row - 1;
                    Tile tileParent = ServerMain.getInstance().getGameManager().getGameWorldProcessor()
                            .getGameWorld(worldName)
                            .getTile(layerDefinition, tileX, tileY);

                    if (tileParent == null) continue;
                    if (useEraser) {
                        tileParent.removeCollision(tileImage);
                        println(getClass(), "Removing collision from " + tileX + "/" + tileY, false, PRINT_DEBUG);
                    } else {
                        tileParent.addCollision(tileImage);
                        println(getClass(), "Setting collision to " + tileX + "/" + tileY, false, PRINT_DEBUG);
                    }
                }
            }
        }
    }
}

package com.forgestorm.server.game.world.tile.properties;

import java.util.Map;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.maps.*;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.shared.game.world.maps.Floors;
import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import com.forgestorm.shared.game.world.tile.properties.WorldEdit;
import lombok.Getter;
import lombok.Setter;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class InteriorStairsProperty extends AbstractTileProperty implements WorldEdit {

    private Integer stairsDownImageID;

    public InteriorStairsProperty() {
        super(TilePropertyTypes.INTERIOR_STAIRS_PROPERTY);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        Integer stairsDownImageID = (Integer) tileProperties.get("stairsDownImageID");
        if (stairsDownImageID != null) setStairsDownImageID(stairsDownImageID);

        println(getClass(), "stairsDownImageID: " + stairsDownImageID, false, printDebugMessages);
        return this;
    }

    @Override
    public void applyPropertyToWorld(WorldChunk worldChunk, TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY, short worldZ) {
        TileImage stairsDownImage = ServerMain.getInstance().getWorldBuilder().getTileImage(stairsDownImageID);
        short worldYup = (short) (worldY + 1);
        short worldZup = (short) (worldZ + 1);

        if (stairsDownImage == null) {
            println(getClass(), "StairsDown TileImage was null. Fix this for TileProperty: " + tileImage.getFileName() + ", ID: " + tileImage.getImageId());
            return;
        }

        // Auto set the next floors TileImage
        Tile targetTile = worldChunk.getTile(layerDefinition, worldX, worldYup, Floors.getFloor(worldZup));
        targetTile.setTileImage(stairsDownImage);

        // Now create a warp so the player can change floors
//        worldChunk.addTileWarp(
//                new WarpLocation(worldX, worldY, worldZ), // From location
//                new Warp(new Location(worldName, worldX + 1, worldYup, worldZup), MoveDirection.WEST)); // To location

//        Warp warpUp = new Warp(new Location(worldName, worldX + 1, worldYup, worldZup), MoveDirection.EAST);
//        worldChunk.addTileWarp(new WarpLocation(worldX, worldY, worldZ), warpUp);
    }

    @Override
    public void removePropertyFromWorld(WorldChunk worldChunk, TileImage tileImage, LayerDefinition layerDefinition, String worldName, int worldX, int worldY, short worldZ) {
        TileImage stairsDownImage = ServerMain.getInstance().getWorldBuilder().getTileImage(stairsDownImageID);
        short worldYup = (short) (worldY + 1);
        short worldZup = (short) (worldZ + 1);

        if (stairsDownImage == null) {
            println(getClass(), "StairsDown TileImage was null. Fix this for TileProperty: " + tileImage.getFileName() + ", ID: " + tileImage.getImageId());
            return;
        }

        // Get the tile and remove the TileImage
        Tile targetTile = worldChunk.getTile(layerDefinition, worldX, worldYup, Floors.getFloor(worldZup));
        targetTile.removeTileImage();

        // Remove the tile warps
        worldChunk.removeTileWarp(worldX, worldY, worldZ);
        worldChunk.removeTileWarp(worldX, worldYup, worldZup);
    }
}

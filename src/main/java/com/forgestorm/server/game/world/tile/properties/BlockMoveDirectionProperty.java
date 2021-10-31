package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class BlockMoveDirectionProperty extends AbstractTileProperty {

    @Getter
    @Setter
    private Boolean blockWalkNorth, blockWalkSouth, blockWalkWest, blockWalkEast;

    public BlockMoveDirectionProperty() {
        super(TilePropertyTypes.BLOCK_MOVE_DIRECTION);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // 4-way direction blocker
        Boolean blockWalkNorth = (Boolean) tileProperties.get("blockWalkNorth");
        Boolean blockWalkSouth = (Boolean) tileProperties.get("blockWalkSouth");
        Boolean blockWalkWest = (Boolean) tileProperties.get("blockWalkWest");
        Boolean blockWalkEast = (Boolean) tileProperties.get("blockWalkEast");
        if (blockWalkNorth != null) setBlockWalkNorth(blockWalkNorth);
        if (blockWalkSouth != null) setBlockWalkSouth(blockWalkSouth);
        if (blockWalkWest != null) setBlockWalkWest(blockWalkWest);
        if (blockWalkEast != null) setBlockWalkEast(blockWalkEast);

        println(getClass(), "blockWalkNorth: " + blockWalkNorth, false, printDebugMessages);
        println(getClass(), "blockWalkSouth: " + blockWalkSouth, false, printDebugMessages);
        println(getClass(), "blockWalkWest: " + blockWalkWest, false, printDebugMessages);
        println(getClass(), "blockWalkEast: " + blockWalkEast, false, printDebugMessages);

        return this;
    }
}

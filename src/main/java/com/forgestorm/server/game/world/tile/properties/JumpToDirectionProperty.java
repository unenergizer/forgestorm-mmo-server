package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class JumpToDirectionProperty extends AbstractTileProperty {

    @Getter
    @Setter
    private Boolean canJumpNorth, canJumpSouth, canJumpWest, canJumpEast;

    public JumpToDirectionProperty() {
        super(TilePropertyTypes.JUMP_TO_DIRECTION);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Tile side can be jumped off
        Boolean canJumpNorth = (Boolean) tileProperties.get("canJumpNorth");
        Boolean canJumpSouth = (Boolean) tileProperties.get("canJumpSouth");
        Boolean canJumpWest = (Boolean) tileProperties.get("canJumpWest");
        Boolean canJumpEast = (Boolean) tileProperties.get("canJumpEast");
        if (canJumpNorth != null) setCanJumpNorth(canJumpNorth);
        if (canJumpSouth != null) setCanJumpSouth(canJumpSouth);
        if (canJumpWest != null) setCanJumpWest(canJumpWest);
        if (canJumpEast != null) setCanJumpEast(canJumpEast);

        println(getClass(), "canJumpNorth: " + canJumpNorth, false, printDebugMessages);
        println(getClass(), "canJumpSouth: " + canJumpSouth, false, printDebugMessages);
        println(getClass(), "canJumpWest: " + canJumpWest, false, printDebugMessages);
        println(getClass(), "canJumpEast: " + canJumpEast, false, printDebugMessages);

        return this;
    }
}

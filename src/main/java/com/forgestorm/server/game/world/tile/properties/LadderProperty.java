package com.forgestorm.server.game.world.tile.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class LadderProperty extends AbstractTileProperty {

    @Getter
    @Setter
    private Boolean isLadder;

    public LadderProperty() {
        super(TilePropertyTypes.LADDER);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Object is Ladder
        Boolean isLadder = (Boolean) tileProperties.get("isTraversable");
        if (isLadder != null) setIsLadder(isLadder);

        println(getClass(), "isTraversable: " + isLadder, false, printDebugMessages);
        return this;
    }
}

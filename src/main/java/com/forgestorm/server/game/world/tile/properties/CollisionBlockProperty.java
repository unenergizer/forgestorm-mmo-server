package com.forgestorm.server.game.world.tile.properties;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class CollisionBlockProperty extends AbstractTileProperty {

    public CollisionBlockProperty() {
        super(TilePropertyTypes.COLLISION_BLOCK);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {
        println(getClass(), "Tile not traversable.", false, printDebugMessages);
        return this;
    }
}

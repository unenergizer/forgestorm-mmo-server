package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.server.game.world.maps.tile.TileImage;
import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public abstract class AbstractTileProperty {

    @Getter
    private transient final TilePropertyTypes tilePropertyType;

    @Setter
    private transient TileImage tileImage;

    protected AbstractTileProperty(TilePropertyTypes tilePropertyType) {
        this.tilePropertyType = tilePropertyType;
    }

    public abstract AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages);
}

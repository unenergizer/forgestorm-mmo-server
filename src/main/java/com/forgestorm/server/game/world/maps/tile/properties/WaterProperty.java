package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class WaterProperty extends AbstractTileProperty {

    @Getter
    @Setter
    private Boolean isWater;

    public WaterProperty() {
        super(TilePropertyTypes.WATER);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Tile is water
        Boolean isWater = (Boolean) tileProperties.get("isWater");
        if (isWater != null) setIsWater(isWater);

        println(getClass(), "isWater: " + isWater, false, printDebugMessages);

        return this;
    }
}

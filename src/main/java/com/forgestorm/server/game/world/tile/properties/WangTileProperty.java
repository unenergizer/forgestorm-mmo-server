package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class WangTileProperty extends AbstractTileProperty {

    @Getter
    @Setter
    private WangType wangType;

    public WangTileProperty() {
        super(TilePropertyTypes.WANG_TILE);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Wang tiles
        String wangType = (String) tileProperties.get("wangType");
        if (wangType != null) setWangType(WangType.valueOf(wangType));

        println(getClass(), "wangType: " + wangType, false, printDebugMessages);

        return this;
    }

    private enum WangType {
        WANG_16,
        WANT_48
    }

}

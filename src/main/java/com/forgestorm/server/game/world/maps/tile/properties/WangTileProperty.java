package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import com.forgestorm.shared.game.world.maps.tile.wang.BrushSize;
import com.forgestorm.shared.game.world.maps.tile.wang.WangType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class WangTileProperty extends AbstractTileProperty {

    private transient int temporaryWangId;
    private transient String wangRegionNamePrefix;
    private WangType wangType;
    private BrushSize minimalBrushSize;

    public WangTileProperty() {
        super(TilePropertyTypes.WANG_TILE);
    }

    @SuppressWarnings("rawtypes")
    public void printDebug(Class clazz) {
        println(clazz, "temporaryWangId: " + temporaryWangId);
        println(clazz, "wangRegionNamePrefix: " + wangRegionNamePrefix);
        println(clazz, "wangType: " + wangType);
        println(clazz, "minimalBrushSize: " + minimalBrushSize);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {
        println(getClass(), "Tile is a wang tile.", false, printDebugMessages);

        String wangType = (String) tileProperties.get("wangType");
        if (wangType != null) setWangType(WangType.valueOf(wangType));

        String minimalBrushSize = (String) tileProperties.get("minimalBrushSize");
        if (minimalBrushSize != null) setMinimalBrushSize(BrushSize.valueOf(minimalBrushSize));

        return this;
    }
}

package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.CursorDrawType;
import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class CursorDrawOverTileProperty extends AbstractTileProperty {

    private CursorDrawType cursorDrawType = CursorDrawType.NO_DRAWABLE;

    public CursorDrawOverTileProperty() {
        super(TilePropertyTypes.CURSOR_DRAW_OVER_TILE);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Take damage from walking over tile
        String cursorDrawType = (String) tileProperties.get("cursorDrawType");
        if (cursorDrawType != null) setCursorDrawType(CursorDrawType.valueOf(cursorDrawType));

        println(getClass(), "cursorDrawType: " + cursorDrawType, false, printDebugMessages);

        return this;
    }
}

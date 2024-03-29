package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class ContainerProperty extends AbstractTileProperty {

    private Boolean isBreakable;

    public ContainerProperty() {
        super(TilePropertyTypes.INTERACTIVE_CONTAINER);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        Boolean isBreakable = (Boolean) tileProperties.get("isBreakable");
        if (isBreakable != null) setIsBreakable(isBreakable);

        println(getClass(), "isBreakable: " + isBreakable, false, printDebugMessages);
        return this;
    }
}

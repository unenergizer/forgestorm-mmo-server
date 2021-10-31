package com.forgestorm.server.game.world.tile.properties;

import com.forgestorm.server.game.world.maps.DoorManager;
import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class DoorProperty extends AbstractTileProperty {

    // Must match the game-client
    private transient DoorManager.DoorStatus doorStatus = DoorManager.DoorStatus.CLOSED;
    private Integer magicLockingLevel;

    public DoorProperty() {
        super(TilePropertyTypes.DOOR);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        Integer magicLockingLevel = (Integer) tileProperties.get("magicLockingLevel");
        if (magicLockingLevel != null) setMagicLockingLevel(magicLockingLevel);

        println(getClass(), "magicLockingLevel: " + magicLockingLevel, false, printDebugMessages);
        return this;
    }
}

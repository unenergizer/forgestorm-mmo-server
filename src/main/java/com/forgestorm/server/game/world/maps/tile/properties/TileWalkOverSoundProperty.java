package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class TileWalkOverSoundProperty extends AbstractTileProperty {

    private TileWalkSound tileWalkSound;

    public TileWalkOverSoundProperty() {
        super(TilePropertyTypes.WALK_OVER_SOUND);
    }


    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Tile walk sound
        String tileWalkSound = (String) tileProperties.get("tileWalkSound");
        if (tileWalkSound != null) setTileWalkSound(TileWalkSound.valueOf(tileWalkSound));

        println(getClass(), "tileWalkSound: " + tileWalkSound, false, printDebugMessages);

        return this;
    }

    enum TileWalkSound {
        NONE,
        BRICK,
        GRASS,
        SAND,
        STONE
    }
}

package com.forgestorm.server.game.world.maps.tile.properties;

import com.forgestorm.shared.game.world.maps.tile.properties.TilePropertyTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class InteractDamageProperty extends AbstractTileProperty {

    private InteractType interactType;
    private Integer interactDamage;

    public InteractDamageProperty() {
        super(TilePropertyTypes.INTERACT_DAMAGE);
    }

    @Override
    public AbstractTileProperty load(Map<String, Object> tileProperties, boolean printDebugMessages) {

        // Take damage from walking over tile
        String interactType = (String) tileProperties.get("interactType");
        if (interactType != null) setInteractType(InteractType.valueOf(interactType));

        Integer walkOverDamage = (Integer) tileProperties.get("interactDamage");
        if (walkOverDamage != null) setInteractDamage(walkOverDamage);

        println(getClass(), "interactType: " + interactType, false, printDebugMessages);
        println(getClass(), "interactDamage: " + walkOverDamage, false, printDebugMessages);

        return this;
    }

    private enum InteractType {
        BUTTON_CLICK,
        WALK_OVER
    }
}

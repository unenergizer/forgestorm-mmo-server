package com.forgestorm.server.game.rpg.attribute;

import lombok.Getter;

@Getter
public class BaseAttribute {

    private final int baseValue;
    private final int baseMultiplier;

    public BaseAttribute(int baseValue, int baseMultiplier) {
        this.baseValue = baseValue;
        this.baseMultiplier = baseMultiplier;
    }
}

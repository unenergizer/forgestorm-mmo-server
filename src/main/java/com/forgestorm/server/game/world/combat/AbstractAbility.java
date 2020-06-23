package com.forgestorm.server.game.world.combat;

import com.forgestorm.server.game.world.entity.MovingEntity;
import lombok.Getter;

@Getter
public abstract class AbstractAbility {

    private final AbilityType abilityType;

    private final int maxCooldownTime;
    private int cooldownTime;

    private final int rangeMin;
    private final int rangeMax;

    private final boolean stackable;

    private boolean active = false;

    public AbstractAbility(AbilityType abilityType, int rangeMin, int rangeMax, int maxCooldownTime, boolean stackable) {
        this.abilityType = abilityType;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.maxCooldownTime = maxCooldownTime;
        this.stackable = stackable;
    }

    public abstract void doAbilityActions(MovingEntity abilityOwner);

    void processCooldownTime() {
        if (cooldownTime > 0) cooldownTime--;
    }

    public void toggleAbility() {
        if (!active) active = true;
    }

    void resetAbility() {
        cooldownTime = maxCooldownTime;
        active = false;
    }

    boolean isReady() {
        return cooldownTime <= 0;
    }

}

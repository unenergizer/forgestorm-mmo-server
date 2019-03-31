package com.valenguard.server.game.world.combat.abilities;

import com.valenguard.server.game.world.combat.AbilityType;
import com.valenguard.server.game.world.combat.AbstractAbility;
import com.valenguard.server.game.world.entity.MovingEntity;

public class MeleeAttackAbility extends AbstractAbility {

    public MeleeAttackAbility() {
        super(AbilityType.ACTIVE, 0, 2, 3, false);
    }

    @Override
    public void doAbilityActions(MovingEntity abilityOwner) {
        if (abilityOwner.getTargetEntity() == null) return;

    }
}

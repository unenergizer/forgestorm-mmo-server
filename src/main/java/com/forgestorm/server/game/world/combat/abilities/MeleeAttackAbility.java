package com.forgestorm.server.game.world.combat.abilities;

import com.forgestorm.server.game.world.combat.AbilityType;
import com.forgestorm.server.game.world.combat.AbstractAbility;
import com.forgestorm.server.game.world.entity.MovingEntity;

public class MeleeAttackAbility extends AbstractAbility {

    public MeleeAttackAbility() {
        super(AbilityType.ACTIVE, 0, 2, 3, false);
    }

    @Override
    public void doAbilityActions(MovingEntity abilityOwner) {
        if (abilityOwner.getTargetEntity() == null) return;

    }
}

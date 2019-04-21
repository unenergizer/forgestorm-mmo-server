package com.valenguard.server.game.abilities;

import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.io.AbilityLoader;
import com.valenguard.server.util.RandomUtil;

import java.util.Map;

public class AbilityManager {

    private Map<Short, Ability> combatAbilities;

    public void start() {
        combatAbilities = new AbilityLoader().loadCombatAbilities();
    }

    public void performAbility(short abilityId, MovingEntity casterEntity, MovingEntity targetEntity) {
        Ability ability = combatAbilities.get(abilityId);
        if (ability == null) return;
        if (casterEntity.isAbilityOnCooldown(ability)) {
            if (casterEntity instanceof Player) {
                ((Player) casterEntity).getQueuedAbilities().put(abilityId, new WaitingAbility(targetEntity));
            }
            return;
        }

        switch (ability.getAbilityType()) {
            case MELEE_ATTACK:
                performMeleeAbility(ability, casterEntity, targetEntity);
                break;
            case RANGE_ATTACK:
                performRangeAbility(ability, casterEntity, targetEntity);
                break;
        }

        if (ability.getCooldown() != null) casterEntity.addAbilityCooldown(ability);
    }

    private void performMeleeAbility(Ability ability, MovingEntity casterEntity, MovingEntity targetEntity) {
        Location casterLocation = casterEntity.getFutureMapLocation();
        Location targetLocation = targetEntity.getFutureMapLocation();

        // TODO: If they have distance specifications then check against those instead! (The default check should be based on distance of weapon used if melee)
        if (!casterLocation.isWithinDistance(targetLocation, (short) 1)) return;

        // TODO: get melee defence off of armor of target

        // TODO: If successful, put in combat
        setInCombat(casterEntity, targetEntity);

        // todo: now do calculations
        int damageToDeal = RandomUtil.getNewRandom(ability.getDamageMin(), ability.getDamageMax());
        targetEntity.dealDamage(damageToDeal, casterEntity);

    }

    private void performRangeAbility(Ability ability, MovingEntity casterEntity, MovingEntity targetEntity) {
        Location casterLocation = casterEntity.getFutureMapLocation();
        Location targetLocation = targetEntity.getFutureMapLocation();

        int distanceAway = casterLocation.getDistanceAway(targetLocation);
        if (distanceAway >= ability.getDistanceMin() && distanceAway <= ability.getDistanceMax()) {

            setInCombat(casterEntity, targetEntity);

            int damageToDeal = RandomUtil.getNewRandom(ability.getDamageMin(), ability.getDamageMax());
            targetEntity.dealDamage(damageToDeal, casterEntity);
        }

    }


    private void setInCombat(MovingEntity attackerEntity, MovingEntity targetEntity) {
        if (!attackerEntity.isInCombat()) {
            attackerEntity.setTargetEntity(targetEntity);
            attackerEntity.setInCombat(true);
        }
        if (!targetEntity.isInCombat()) {
            targetEntity.setTargetEntity(attackerEntity); // TODO: we should set targets based on highest damage and the like
            targetEntity.setInCombat(true);
        }
    }
}
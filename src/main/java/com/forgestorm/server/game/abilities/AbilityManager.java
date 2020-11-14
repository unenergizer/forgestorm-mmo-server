package com.forgestorm.server.game.abilities;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.util.RandomUtil;

import java.util.Map;

public class AbilityManager implements ManagerStart {

    private Map<Short, Ability> combatAbilities;
    private Ability genericAiEntityAbility;

    @Override
    public void start() {
        ServerMain.getInstance().getFileManager().loadAbilityData();
        combatAbilities = ServerMain.getInstance().getFileManager().getAbilityData().getCombatAbilitiesMap();
        genericAiEntityAbility = new Ability();
        genericAiEntityAbility.setAbilityId((short) -1);
        genericAiEntityAbility.setAbilityType(AbilityType.MELEE_ATTACK);
        genericAiEntityAbility.setCooldown(2);
        genericAiEntityAbility.setDamageMin(1);
        genericAiEntityAbility.setDamageMax(1);
        genericAiEntityAbility.setDistanceMin(0);
        genericAiEntityAbility.setDistanceMax(1);
    }

    public void performAiEntityAbility(AiEntity aiEntity, MovingEntity targetEntity) {
        genericAiEntityAbility.setDamageMax(aiEntity.getAttributes().getDamage());
        performAbility(genericAiEntityAbility, aiEntity, targetEntity);
    }

    public void performPlayerAbility(short abilityId, MovingEntity casterEntity, MovingEntity targetEntity) {
        performAbility(combatAbilities.get(abilityId), casterEntity, targetEntity);
    }

    private void performAbility(Ability ability, MovingEntity casterEntity, MovingEntity targetEntity) {
        if (ability == null) return;
        if (casterEntity.isAbilityOnCooldown(ability)) {
            if (casterEntity instanceof Player) {
                ((Player) casterEntity).getQueuedAbilities().put(ability.getAbilityId(), new WaitingAbility(targetEntity));
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
        Location casterLocation = casterEntity.getFutureWorldLocation();
        Location targetLocation = targetEntity.getFutureWorldLocation();

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
        Location casterLocation = casterEntity.getFutureWorldLocation();
        Location targetLocation = targetEntity.getFutureWorldLocation();

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

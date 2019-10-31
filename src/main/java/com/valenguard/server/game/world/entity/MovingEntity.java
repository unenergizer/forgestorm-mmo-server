package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.abilities.Ability;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.combat.AbstractAbility;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityDamagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;
import com.valenguard.server.util.MathUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MovingEntity extends Entity {

    /**
     * The exact tile location of the entity on the tile grid.
     */
    private Location futureMapLocation;

    /**
     * The direction the entity is facing. Is not always the same direction
     * as they are moving because the move direction can be NONE.
     */
    private MoveDirection facingDirection;

    /**
     * The rate of speed the entity moves across tiles.
     * The smaller the number, the faster the entity moves.
     */
    private float moveSpeed;

    /**
     * Current X and Y of the packetReceiver (this is the interpolated values)
     */
    private float realX, realY;

    /**
     * Used by entity manager to measure the walk time between tiles/locations.
     */
    private float walkTime = 0f;

    /**
     * Entity attributes
     */
    private Attributes attributes = new Attributes();

    private int maxHealth;
    private int currentHealth;

    /**
     * This is the entity that we are interested in
     */
    private MovingEntity targetEntity;

    private MoveDirection previousDirection = MoveDirection.NONE;

    private final List<AbstractAbility> abstractAbilityList = new ArrayList<>();

    private boolean inCombat = false;

    private Map<Short, Integer> abilitiesToCooldown = new HashMap<>();

    /**
     * The amount of time that has progressed since the player
     * has been out of active.
     */
    private int combatIdleTime;

    public void setInCombat(boolean isInCombat) {
        if (isInCombat) combatIdleTime = 0;
        this.inCombat = isInCombat;
    }

    public void addAbilityCooldown(Ability ability) {
        // Ability cooldown time * 20 ticks (20 ticks = 1 second)
//        println(getClass(), "Adding a cooldown with time: " + (ability.getCooldown() * GameConstants.TICKS_PER_SECOND));
        abilitiesToCooldown.put(ability.getAbilityId(), ability.getCooldown() * GameConstants.TICKS_PER_SECOND);
    }

    public boolean isAbilityOnCooldown(Ability ability) {
        return abilitiesToCooldown.containsKey(ability.getAbilityId());
    }

    public void gameMapRegister(Warp warp) {
        setCurrentMapLocation(new Location(warp.getLocation()));
        setFutureMapLocation(new Location(warp.getLocation()));
        setRealX(warp.getLocation().getX() * GameConstants.TILE_SIZE);
        setRealY(warp.getLocation().getY() * GameConstants.TILE_SIZE);
        walkTime = 0f;
        setFacingDirection(warp.getFacingDirection());
    }

    void gameMapDeregister() {
        setWalkTime(0f);
    }

    public void heal(int amount) {
        // Ensuring not to exceed the maximum health.
        final int realAmount = amount + currentHealth > maxHealth ? maxHealth - currentHealth : amount;
        getGameMap().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityHealPacketOut(anyPlayer, this, realAmount).sendPacket());
        currentHealth += realAmount;
    }

    public void dealDamage(int amount, MovingEntity attackerEntity) {

        int armor = this.getAttributes().getArmor();
        int damage = MathUtil.applyArmor(amount, armor);

        // Don't let damage go into negatives
        if (damage < 0) damage = 0;

        // Prevent health from going below zero. It causes graphical bugs in client.
        if (currentHealth - damage <= 0) currentHealth = 0;
        else currentHealth -= damage;


        int finalDamage = damage;
        getGameMap().getPlayerController().forAllPlayers(player ->
                new EntityDamagePacketOut(player, this, currentHealth, finalDamage).sendPacket());
        if (this instanceof Player) {
            new ChatMessagePacketOut((Player) this, "[RED]" + attackerEntity.getName() + " hit you for " + damage + ".").sendPacket();
        }
        if (attackerEntity instanceof Player) {
            new ChatMessagePacketOut((Player) attackerEntity, "You hit " + getName() + " for " + damage + ".").sendPacket();
        }
    }

    public boolean isEntityMoving() {
        return getCurrentMapLocation().getX() != getFutureMapLocation().getX() || getCurrentMapLocation().getY() != getFutureMapLocation().getY();
    }

    public void clearCombatTargets() {
        if (targetEntity != null && targetEntity.getTargetEntity() == this)
            targetEntity.setTargetEntity(null); // Clear other entity target
        setTargetEntity(null); // Clear this monsters target
    }
}

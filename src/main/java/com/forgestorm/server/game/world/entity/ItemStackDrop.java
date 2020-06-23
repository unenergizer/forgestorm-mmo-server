package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.world.item.ItemStack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemStackDrop extends Entity {

    /**
     * The item that was dropped.
     */
    private ItemStack itemStack;

    /**
     * The player that is first to see the item drop.
     */
    private Player dropOwner;

    /**
     * Determines if the item was picked up or not.
     */
    private boolean pickedUp = false;

    /**
     * If enough time has passed, we set this to true so all players can see the dropped item.
     */
    private boolean spawnedForAll = false;

    /**
     * Denotes if this item was spawned by a monster kill. If it was, we will use a timer to
     * remove the item from the ground after a certain amount of time. Otherwise we will
     * assume this is an item from the Database that will spawn every time the server restarts.
     */
    private boolean spawnedFromMonster = true;
    private int respawnTimeMin;
    private int respawnTimeMax;


    public void removeItemStackDrop() {
        getCurrentMapLocation().getGameMap().getItemStackDropEntityController().queueEntityDespawn(this);
    }
}

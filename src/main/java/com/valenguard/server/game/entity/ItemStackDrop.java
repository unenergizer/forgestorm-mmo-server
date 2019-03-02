package com.valenguard.server.game.entity;

import com.valenguard.server.game.inventory.ItemStack;
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
    private Player killer;

    /**
     * Determines if the item was picked up or not.
     */
    private boolean pickedUp = false;

    /**
     * If enough time has passed, we set this to true so all players can see the dropped item.
     */
    private boolean spawnedForAll = false;

}

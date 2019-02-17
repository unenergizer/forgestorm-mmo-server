package com.valenguard.server.game.entity;

import com.valenguard.server.game.inventory.ItemStack;
import lombok.Getter;
import lombok.Setter;

public class ItemStackDrop extends Entity {

    private int timeTillPublicDrop = 60;
    @Setter
    @Getter
    private ItemStack itemStack;

    public boolean doItemStackDropTick() {
        timeTillPublicDrop--;
        return timeTillPublicDrop <= 0;
    }

}

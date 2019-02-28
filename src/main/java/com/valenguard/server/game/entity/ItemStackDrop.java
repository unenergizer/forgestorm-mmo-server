package com.valenguard.server.game.entity;

import com.valenguard.server.game.inventory.ItemStack;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ItemStackDrop extends Entity {

    private ItemStack itemStack;

    private Player killer;

    private boolean pickedUp;

}

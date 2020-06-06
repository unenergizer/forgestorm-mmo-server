package com.valenguard.server.game.world.item;

import com.valenguard.server.game.world.entity.Player;

public class ItemStackConsumerManager {

    public void consumeItem(Player player, ItemStack itemStack) {
        if (itemStack.getItemStackType() == ItemStackType.POTION) {
            consumePotions(player, itemStack);
        }
    }

    private void consumePotions(Player player, ItemStack itemStack) {
        if (itemStack.getItemId() == 18) { // Small health potion
            player.heal(25);
        } else if (itemStack.getItemId() == 19) { // Median health potion
            player.heal(100);
        } else if (itemStack.getItemId() == 20) { // Large health potion
            player.heal(250);
        }
    }

}

package com.forgestorm.server.game.world.item;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.shared.game.world.item.ItemStackType;

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

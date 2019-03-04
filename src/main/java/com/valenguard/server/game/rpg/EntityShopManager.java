package com.valenguard.server.game.rpg;

import com.valenguard.server.game.data.EntityShopLoader;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.ItemStackSlotData;
import com.valenguard.server.game.inventory.ShopItemStackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private Map<Short, List<ShopItemStackInfo>> map = new HashMap<>();

    public EntityShopManager() {
        init();
    }

    private void init() {
        map = EntityShopLoader.loadFromFile();
    }

    public void buyItem(short shopID, short shopSlot, Player player) {
        ItemStackSlotData itemStackSlotData = player.getGold();
        if (itemStackSlotData == null) return; // The player has no gold.

        int buyPrice = map.get(shopID).get(shopSlot).getPrice();

        // The player cannot buy items if they don't have the space for the item.
        if (player.getPlayerBag().isBagFull()) return;

        // The player does not have enough gold.
        if (buyPrice > itemStackSlotData.getItemStack().getAmount()) return;

        ItemStack newGoldStack = itemStackSlotData.getItemStack();
        newGoldStack.setAmount(newGoldStack.getAmount() - buyPrice);

        player.removeItemStack(itemStackSlotData.getBagIndex());

        // Only adding the gold stack back if it is greater than zero.
        if (newGoldStack.getAmount() > 0) {
            player.giveItemStack(newGoldStack);
        }

    }

    public void sellItem(short shopID, Player player) {

    }
}

package com.valenguard.server.game.rpg;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackSlotData;
import com.valenguard.server.io.EntityShopLoader;

import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private Map<Short, List<EntityShopLoader.ShopItemStackInfo>> map = new EntityShopLoader().loadFromFile();

    public void buyItem(short shopID, short shopSlot, Player player) {
        ItemStackSlotData itemStackSlotData = player.getGold();
        if (itemStackSlotData == null) return; // The player has no gold.

        if (shopSlot >= map.get(shopID).size() || shopSlot < 0) return;

        EntityShopLoader.ShopItemStackInfo shopItemStackInfo = map.get(shopID).get(shopSlot);
        int buyPrice = shopItemStackInfo.getPrice();
        int itemId = shopItemStackInfo.getItemId();

        // The player cannot buy items if they don't have the space for the item.
        if (player.getPlayerBag().isBagFull()) return;

        // The player does not have enough gold.
        if (buyPrice > itemStackSlotData.getItemStack().getAmount()) return;

        ItemStack newGoldStack = itemStackSlotData.getItemStack();
        newGoldStack.setAmount(newGoldStack.getAmount() - buyPrice);

        player.removeItemStackFromBag(itemStackSlotData.getBagIndex());

        // Only adding the gold stack back if it is greater than zero.
        if (newGoldStack.getAmount() > 0) {
            player.setItemStack(itemStackSlotData.getBagIndex(), newGoldStack);
        }

        player.giveItemStack(Server.getInstance().getItemStackManager().makeItemStack(itemId, 1));
    }

    public void sellItem(short shopID, Player player) {

    }
}

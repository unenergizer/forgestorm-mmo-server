package com.valenguard.server.game.rpg;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.io.EntityShopLoader;

import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private Map<Short, List<EntityShopLoader.ShopItemStackInfo>> map = new EntityShopLoader().loadFromFile();

    // TODO: add playerSellItemStack functionality

    public void playerBuyItemStack(short shopID, short shopSlot, Player player) {
        InventorySlot inventorySlot = player.getPlayerBag().getGoldInventorySlot();
        if (inventorySlot == null) return; // The player has no gold.

        if (shopSlot >= map.get(shopID).size() || shopSlot < 0) return;

        EntityShopLoader.ShopItemStackInfo shopItemStackInfo = map.get(shopID).get(shopSlot);
        int buyPrice = shopItemStackInfo.getPrice();
        int itemId = shopItemStackInfo.getItemId();

        // The player cannot buy items if they don't have the space for the item.
        if (player.getPlayerBag().isBagFull()) return;

        // The player does not have enough gold.
        if (buyPrice > inventorySlot.getItemStack().getAmount()) return;

        ItemStack newGoldStack = inventorySlot.getItemStack();
        newGoldStack.setAmount(newGoldStack.getAmount() - buyPrice);

        // Only adding the gold stack back if it is greater than zero.
        if (newGoldStack.getAmount() > 0) {
            player.getPlayerBag().setItemStack(inventorySlot.getSlotIndex(), newGoldStack, true);
        } else {
            player.getPlayerBag().removeItemStack(inventorySlot.getSlotIndex(), true);
        }

        player.getPlayerBag().giveItemStack(Server.getInstance().getItemStackManager().makeItemStack(itemId, 1), true);
    }
}

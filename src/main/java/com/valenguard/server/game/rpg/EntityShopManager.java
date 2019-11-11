package com.valenguard.server.game.rpg;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.io.EntityShopLoader;

import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private final Map<Short, List<EntityShopLoader.ShopItemStackInfo>> entityShopMap = new EntityShopLoader().loadFromFile();

    // TODO: add playerSellItemStack functionality

    public void playerBuyItemStack(short shopID, short shopSlot, Player player) {
        InventorySlot inventorySlot = player.getPlayerBag().getGoldInventorySlot();
        if (inventorySlot == null) return; // The player has no gold.

        // Make sure the player can only buy items within a certain distance.
        if (!player.getCurrentShoppingEntity().getFutureMapLocation().isWithinDistance(player, (short) 5)) {
            player.setCurrentShoppingEntity(null);
            return;
        }

        if (shopSlot >= entityShopMap.get(shopID).size() || shopSlot < 0) return;

        EntityShopLoader.ShopItemStackInfo shopItemStackInfo = entityShopMap.get(shopID).get(shopSlot);
        int buyPrice = shopItemStackInfo.getPrice();
        int itemId = shopItemStackInfo.getItemId();

        // The player cannot buy items if they don't have the space for the item.
        if (player.getPlayerBag().isInventoryFull()) return;

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

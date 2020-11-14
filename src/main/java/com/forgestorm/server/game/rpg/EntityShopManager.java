package com.forgestorm.server.game.rpg;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.inventory.InventorySlot;
import com.forgestorm.server.io.EntityShopLoader;

import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private final Map<Short, List<EntityShopLoader.ShopItemStackInfo>> entityShopMap = new EntityShopLoader().loadFromFile();

    // TODO: add playerSellItemStack functionality

    public void playerBuyItemStack(short shopID, short shopSlot, Player player) {
        List<InventorySlot> goldSlots = player.getAllGoldSlots();

        // Make sure the player can only buy items within a certain distance.
        if (!player.getCurrentShoppingEntity().getFutureWorldLocation().isWithinDistance(player, GameConstants.MAX_SHOP_DISTANCE)) {
            player.setCurrentShoppingEntity(null);
            return;
        }

        if (shopSlot >= entityShopMap.get(shopID).size() || shopSlot < 0) return;

        EntityShopLoader.ShopItemStackInfo shopItemStackInfo = entityShopMap.get(shopID).get(shopSlot);
        int buyPrice = shopItemStackInfo.getPrice();
        int itemId = shopItemStackInfo.getItemId();

        // The player cannot buy items if they don't have the space for the item.
        boolean bagFull = player.getPlayerBag().isInventoryFull();
        boolean hotBarFull = player.getPlayerHotBar().isInventoryFull();
        if (bagFull && hotBarFull) return;

        // The player does not have enough gold.
        if (buyPrice > getGoldAmount(goldSlots)) {
            System.out.println("DO NOT HAVE ENOUGH GOLD");
            System.out.println("BUY PRICE: " + buyPrice);
            System.out.println("GOLD AMOUNT: " + getGoldAmount(goldSlots));
            return;
        }

        removeGold(goldSlots, buyPrice);

        player.give(ServerMain.getInstance().getItemStackManager().makeItemStack(itemId, 1), true);
    }

    private void removeGold(List<InventorySlot> goldSlots, final int buyPrice) {
        int runningTotal = 0;

        // 100
        // 0
        // 101
        //

        for (InventorySlot slot : goldSlots) {
            int stackAmount = slot.getItemStack().getAmount();
            if (buyPrice - runningTotal >= stackAmount) {
                runningTotal += slot.getItemStack().getAmount();
                slot.getInventory().removeItemStack(slot.getSlotIndex(), true);
                System.out.println("WE REMOVING FOR TYPE: " + slot.getInventory().getInventoryType());
                if (runningTotal == buyPrice) break;
            } else {
                ItemStack newGoldStack = slot.getItemStack();
                newGoldStack.setAmount(stackAmount - (buyPrice - runningTotal));
                slot.getInventory().setItemStack(slot.getSlotIndex(), newGoldStack, true);
                break;
            }
        }
    }

    private int getGoldAmount(List<InventorySlot> goldSlots) {
        int total = 0;
        for (InventorySlot slot : goldSlots) {
            total += slot.getItemStack().getAmount();
        }
        return total;
    }
}

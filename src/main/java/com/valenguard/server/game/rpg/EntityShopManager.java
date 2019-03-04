package com.valenguard.server.game.rpg;

import com.valenguard.server.game.data.EntityShopLoader;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.ItemStack;
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
        int buyPrice = map.get(shopID).get(shopSlot).getPrice();

    }

    public void sellItem(short shopID, Player player) {

    }
}

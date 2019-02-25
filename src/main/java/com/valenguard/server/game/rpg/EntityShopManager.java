package com.valenguard.server.game.rpg;

import com.valenguard.server.game.data.EntityShopLoader;
import com.valenguard.server.game.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityShopManager {

    private Map<Short, List<Integer>> map = new HashMap<>();

    public EntityShopManager() {
        init();
    }

    private void init() {
        map = EntityShopLoader.loadFromFile();
    }

    public void buyItem(short shopID, short shopSlot, Player player) {

    }

    public void sellItem(short shopID, Player player) {

    }

    public Integer getItemForShop(short shopID, int itemStackShopSlot) {
        if (!map.containsKey(shopID)) return null;
        if (itemStackShopSlot > map.get(shopID).size() - 1) return null;
        return map.get(shopID).get(itemStackShopSlot);
    }

    public List<Integer> getShopItemList(short shopID) {
        if (!map.containsKey(shopID)) return null;
        return map.get(shopID);
    }
}

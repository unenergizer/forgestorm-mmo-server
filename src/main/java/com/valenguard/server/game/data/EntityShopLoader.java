package com.valenguard.server.game.data;

import com.valenguard.server.game.inventory.ShopItemStackInfo;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityShopLoader {

    private static final String FILE_PATH = "src/main/resources/data/item/ShopItems.yaml";

    public static Map<Short, List<ShopItemStackInfo>> loadFromFile() {

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<Integer, Map<String, Object>>> root = yaml.load(inputStream);

        Map<Short, List<ShopItemStackInfo>> map = new HashMap<>();

        short count = 0;
        for (Map<Integer, Map<String, Object>> shopObject : root.values()) {
            List<ShopItemStackInfo> shopItemStackInfos = new ArrayList<>();
            for (Map<String, Object> shopItemObject : shopObject.values()) {
                ShopItemStackInfo itemStackInfo = new ShopItemStackInfo(
                        (Integer) shopItemObject.get("id"),
                        (Integer) shopItemObject.get("price"));
                shopItemStackInfos.add(itemStackInfo);
            }
            map.put(count++, shopItemStackInfos);
        }

        return map;
    }
}

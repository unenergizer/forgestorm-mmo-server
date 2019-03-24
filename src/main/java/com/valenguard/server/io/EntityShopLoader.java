package com.valenguard.server.io;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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

    public Map<Short, List<ShopItemStackInfo>> loadFromFile() {

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.ENTITY_SHOP.getFilePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<Integer, Map<String, Object>>> root = yaml.load(inputStream);

        Map<Short, List<ShopItemStackInfo>> map = new HashMap<>();

        short count = 0;
        for (Map<Integer, Map<String, Object>> shopObject : root.values()) {
            List<ShopItemStackInfo> shopItemStackInfo = new ArrayList<>();
            for (Map<String, Object> shopItemObject : shopObject.values()) {
                ShopItemStackInfo itemStackInfo = new ShopItemStackInfo(
                        (Integer) shopItemObject.get("id"),
                        (Integer) shopItemObject.get("price"));
                shopItemStackInfo.add(itemStackInfo);
            }
            map.put(count++, shopItemStackInfo);
        }

        return map;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class ShopItemStackInfo {
        private int itemId;
        private int price;
    }
}

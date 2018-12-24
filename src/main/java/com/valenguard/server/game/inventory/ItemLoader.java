package com.valenguard.server.game.inventory;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemLoader {

    public List<ItemStack> loadItems() {
        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File("src/main/resources/item/items.yaml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<String, Map<String, Object>> root = yaml.load(inputStream);

        List<ItemStack> itemStacks = new ArrayList<ItemStack>();

        for (Map.Entry<String, Map<String, Object>> entry : root.entrySet()) {
            int itemId = Integer.parseInt(entry.getKey());
            Map<String, Object> itemNode = entry.getValue();

            ItemStack itemStack = new ItemStack(itemId);

            String name = (String) itemNode.get("name");
            String desc = (String) itemNode.get("desc");
            ItemStackType type = ItemStackType.valueOf((String) itemNode.get("type"));

            Integer wearable = (Integer) itemNode.get("wearable");
            if (wearable != null) {
                itemStack = new WearableItemStack(itemId);
                ((WearableItemStack) itemStack).setTextureId(wearable.shortValue());
            }

            itemStack.setName(name);
            itemStack.setDescription(desc);
            itemStack.setItemStackType(type);
            itemStack.setAmount(-1);

            itemStacks.add(itemStack);
        }

        return itemStacks;
    }

}

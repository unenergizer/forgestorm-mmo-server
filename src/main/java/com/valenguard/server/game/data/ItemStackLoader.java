package com.valenguard.server.game.data;

import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.ItemStackType;
import com.valenguard.server.game.inventory.WearableItemStack;
import com.valenguard.server.game.rpg.Attributes;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class ItemStackLoader {

    private static final boolean PRINT_DEBUG = false;
    private static final String FILE_PATH = "src/main/resources/data/item/Items.yaml";

    public List<ItemStack> loadItems() {
        println(getClass(), "====== START LOADING ITEMS ======", false, PRINT_DEBUG);

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FILE_PATH));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Integer, Map<String, Object>> root = yaml.load(inputStream);

        List<ItemStack> itemStacks = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Object>> entry : root.entrySet()) {
            int itemId = entry.getKey();
            Map<String, Object> itemNode = entry.getValue();

            ItemStack itemStack = new ItemStack(itemId);

            /*
             * Get universal item information
             */
            String name = (String) itemNode.get("name");
            String desc = (String) itemNode.get("desc");
            ItemStackType type = ItemStackType.valueOf((String) itemNode.get("type"));
            String region = (String) itemNode.get("region");

            /*
             * Get wearable item data
             */
            Integer wearable = (Integer) itemNode.get("wearable");
            if (wearable != null) {
                itemStack = new WearableItemStack(itemId);
                ((WearableItemStack) itemStack).setTextureId(wearable.shortValue());
            }

            /*
             * Get item stats
             */
            Attributes attributes = new Attributes();
            Integer stat;

            stat = (Integer) itemNode.get("damage");
            if (stat != null) attributes.setDamage(stat);

            stat = (Integer) itemNode.get("armor");
            if (stat != null) attributes.setArmor(stat);

            itemStack.setName(name);
            itemStack.setDescription(desc);
            itemStack.setItemStackType(type);
            itemStack.setAmount(-1);
            itemStack.setAttributes(attributes);

            println(getClass(), "ID: " + itemId, false, PRINT_DEBUG);
            println(getClass(), "Name: " + name, false, PRINT_DEBUG);
            println(getClass(), "Description: " + desc, false, PRINT_DEBUG);
            println(getClass(), "ItemStackType: " + type, false, PRINT_DEBUG);
            println(getClass(), "Region: " + region, false, PRINT_DEBUG);

            println(getClass(), "Damage: " + attributes.getDamage(), false, PRINT_DEBUG && attributes.getDamage() != 0);
            println(getClass(), "Armor: " + attributes.getArmor(), false, PRINT_DEBUG && attributes.getArmor() != 0);

            println(PRINT_DEBUG);

            itemStacks.add(itemStack);
        }

        println(getClass(), "====== END LOADING ITEMS ======", false, PRINT_DEBUG);
        return itemStacks;
    }
}

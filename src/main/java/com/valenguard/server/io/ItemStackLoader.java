package com.valenguard.server.io;

import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.game.world.item.WearableItemStack;
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

    public List<ItemStack> loadItems() {
        println(getClass(), "====== START LOADING ITEMS ======", false, PRINT_DEBUG);

        Yaml yaml = new Yaml();

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(FilePaths.ITEM_STACK.getFilePath()));
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
            Integer stackable = (Integer) itemNode.get("stackable");
            boolean isConsumable = (Boolean) itemNode.get("consume");

            /*
             * Get wearable item io
             */
            Integer wearable = (Integer) itemNode.get("wearable");
            if (wearable != null) {
                itemStack = new WearableItemStack(itemId);
                ((WearableItemStack) itemStack).setTextureId(wearable.byteValue());
            }

            Integer color = (Integer) itemNode.get("color");
            if (type == ItemStackType.GLOVES && color != null) {
                itemStack = new WearableItemStack(itemId);
                ((WearableItemStack) itemStack).setColor(color);
            }

            /*
             * Get Skill Information
             */
            Integer skillID = (Integer) itemNode.get("skillID");
            if (skillID != null) {
                itemStack.setSkillID(skillID);
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

            if (stackable != null) {
                itemStack.setStackable(stackable);
            } else {
                itemStack.setStackable(1);
            }

            itemStack.setAmount(-1);
            itemStack.setAttributes(attributes);
            itemStack.setConsumable(isConsumable);

            println(getClass(), "ID: " + itemId, false, PRINT_DEBUG);
            println(getClass(), "Name: " + name, false, PRINT_DEBUG);
            println(getClass(), "Description: " + desc, false, PRINT_DEBUG);
            println(getClass(), "ItemStackType: " + type, false, PRINT_DEBUG);

            println(getClass(), "Damage: " + attributes.getDamage(), false, PRINT_DEBUG && attributes.getDamage() != 0);
            println(getClass(), "Armor: " + attributes.getArmor(), false, PRINT_DEBUG && attributes.getArmor() != 0);
            if (itemStack instanceof WearableItemStack) {
                println(getClass(), "TextureId: " + ((WearableItemStack) itemStack).getTextureId(), false, PRINT_DEBUG);
                println(getClass(), "Color: " + ((WearableItemStack) itemStack).getColor(), false, PRINT_DEBUG);
            }
            println(getClass(), "SkillId: " + skillID, false, PRINT_DEBUG && skillID != null);
            println(PRINT_DEBUG);

            itemStacks.add(itemStack);
        }

        println(getClass(), "====== END LOADING ITEMS ======", false, PRINT_DEBUG);
        return itemStacks;
    }
}

package com.valenguard.server.game.world.item;

import com.valenguard.server.Server;
import com.valenguard.server.io.DropTableLoader;

import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.valenguard.server.util.Log.println;

public class DropTableManager {

    private DropTableLoader.DropTable[] dropTables;

    private Random random = new Random();

    public void start() {
        DropTableLoader dropTableLoader = new DropTableLoader();
        List<DropTableLoader.DropTable> loadedDropTables = dropTableLoader.loadDropTables();
        dropTables = new DropTableLoader.DropTable[loadedDropTables.size()];
        loadedDropTables.toArray(dropTables);
    }

    public ItemStack getItemStack(Integer dropTableID, int amount) {

        println(getClass(), "Getting item from drop table: " + dropTableID);

        DropTableLoader.DropTable dropTable = dropTables[dropTableID];
        int[] itemStackIDs = dropTable.getItemStackIDs();
        float[] probabilities = dropTable.getProbabilities();

        for (float f : probabilities) {
            println(getClass(), "Probability: " + f);
        }

        for (int id : itemStackIDs) {
            println(getClass(), "item ID: " + id);
        }

        for (int i = 0; i < itemStackIDs.length - 1; i++) {

            println(getClass(), "Must be greater than: " + random.nextFloat() * 100);
            println(getClass(), "The probability: " + probabilities[i]);

            if (random.nextFloat() * 100 < probabilities[i]) {
                return makeItemStack(itemStackIDs[i], amount);
            }
        }

        //Integer itemStackID = dropTables[dropTableID].getItemStackID();

        //checkNotNull(itemStackID, "ItemStack IDs cannot be null!");

        return makeItemStack(itemStackIDs[itemStackIDs.length - 1], amount);
    }

    private ItemStack makeItemStack(int itemStackID, int amount) {
        ItemStackManager itemStackManager = Server.getInstance().getItemStackManager();
        ItemStack itemStack = itemStackManager.makeItemStack(itemStackID, amount);
        itemStack.setAmount(amount);
        return itemStack;
    }
}

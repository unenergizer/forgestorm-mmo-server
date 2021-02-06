package com.forgestorm.server.game.world.item;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.io.DropTableLoader;

import java.util.List;
import java.util.Random;

import static com.forgestorm.server.util.Log.println;

public class DropTableManager implements ManagerStart {

    private static final boolean PRINT_DEBUG = false;

    private final Random random = new Random();

    private DropTableLoader.DropTable[] dropTables;

    @Override
    public void start() {
        DropTableLoader dropTableLoader = new DropTableLoader();
        List<DropTableLoader.DropTable> loadedDropTables = dropTableLoader.loadDropTables();
        dropTables = new DropTableLoader.DropTable[loadedDropTables.size()];
        loadedDropTables.toArray(dropTables);
    }

    public ItemStack[] getItemStack(Integer dropTableID, int amount) {

        if (dropTableID > dropTables.length) {
            println(getClass(), "Tried to use a DropTable that doesn't exist... DropTable: " + dropTableID, true);
            dropTableID = 0; // Setting to default drop table
        }

        DropTableLoader.DropTable dropTable = dropTables[dropTableID];
        int[] itemStackIDs = dropTable.getItemStackIDs();
        float[] probabilities = dropTable.getProbabilities();

        println(getClass(), "Number of items: " + itemStackIDs.length, false, PRINT_DEBUG);

        ItemStack[] itemStacks = new ItemStack[itemStackIDs.length];

        for (int i = 0; i < itemStackIDs.length; i++) {
            if (random.nextFloat() * 100 < probabilities[i]) {
                itemStacks[i] = makeItemStack(itemStackIDs[i], amount);
            }
        }

        return itemStacks;
    }

    private ItemStack makeItemStack(int itemStackID, int amount) {
        ItemStackManager itemStackManager = ServerMain.getInstance().getItemStackManager();
        ItemStack itemStack = itemStackManager.makeItemStack(itemStackID, amount);
        itemStack.setAmount(amount);
        return itemStack;
    }
}

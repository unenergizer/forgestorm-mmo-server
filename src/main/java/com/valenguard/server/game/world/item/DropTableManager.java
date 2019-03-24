package com.valenguard.server.game.world.item;

import com.valenguard.server.Server;
import com.valenguard.server.io.DropTableLoader;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DropTableManager {

    private DropTableLoader.DropTable[] dropTables;

    public void start() {
        DropTableLoader dropTableLoader = new DropTableLoader();
        List<DropTableLoader.DropTable> loadedDropTables = dropTableLoader.loadDropTables();
        dropTables = new DropTableLoader.DropTable[loadedDropTables.size()];
        loadedDropTables.toArray(dropTables);
    }

    public ItemStack dropItemOnMap(Integer dropTableID, int amount) {
        Integer itemStackID = dropTables[dropTableID].getItemStackID();

        checkNotNull(itemStackID, "ItemStack IDs cannot be null!");

        ItemStackManager itemStackManager = Server.getInstance().getItemStackManager();
        ItemStack itemStack = itemStackManager.makeItemStack(itemStackID, amount);
        itemStack.setAmount(amount);
        return itemStack;
    }
}

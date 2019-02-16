package com.valenguard.server.game.inventory;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.data.DropTable;
import com.valenguard.server.game.data.DropTableLoader;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DropTableManager {

    private DropTable[] dropTables;

    public DropTableManager() {
        init();
    }

    private void init() {
        DropTableLoader dropTableLoader = new DropTableLoader();
        List<DropTable> loadedDropTables = dropTableLoader.loadDropTables();
        dropTables = new DropTable[loadedDropTables.size()];
        loadedDropTables.toArray(dropTables);
    }

    public ItemStack dropItemOnMap(Integer dropTableID, int amount) {
        Integer itemStackID = dropTables[dropTableID].getItemStackID();

        checkNotNull(itemStackID, "ItemStack IDs cannot be null!");

        ItemStackManager itemStackManager = ValenguardMain.getInstance().getItemStackManager();
        ItemStack itemStack = itemStackManager.makeItemStack(itemStackID, amount);
        itemStack.setAmount(amount);
        return itemStack;
    }
}

package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class InventoryChangePacket extends ServerOutPacket  {

    private InventoryActions inventoryAction;

    public InventoryChangePacket(Player player, InventoryActions inventoryAction) {
        super(Opcodes.INVENTORY_UPDATE, player);
        this.inventoryAction = inventoryAction;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {

        Log.println(getClass(), "InventoryAction: " + inventoryAction.getInventoryActionType());

        write.writeByte(inventoryAction.getInventoryActionType());

        if (inventoryAction.getInventoryActionType() == InventoryActions.GIVE) {

            Log.println(getClass(), "Item Id: " + inventoryAction.getItemStack().getItemId());
            Log.println(getClass(), "Item Amount: " + inventoryAction.getItemStack().getAmount());

            write.writeInt(inventoryAction.getItemStack().getItemId());
            write.writeInt(inventoryAction.getItemStack().getAmount());
        }
    }
}

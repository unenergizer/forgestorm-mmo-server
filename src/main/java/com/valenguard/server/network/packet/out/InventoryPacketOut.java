package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.network.shared.Opcodes;

public class InventoryPacketOut extends ServerAbstractOutPacket {

    private final InventoryActions inventoryAction;

    public InventoryPacketOut(Player player, InventoryActions inventoryAction) {
        super(Opcodes.INVENTORY_UPDATE, player);
        this.inventoryAction = inventoryAction;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {

        write.writeByte(inventoryAction.getInventoryActionType());

        if (inventoryAction.getInventoryActionType() == InventoryActions.GIVE) {

            write.writeInt(inventoryAction.getItemStack().getItemId());
            write.writeInt(inventoryAction.getItemStack().getAmount());

        } else if (inventoryAction.getInventoryActionType() == InventoryActions.REMOVE) {

            write.writeByte(inventoryAction.getSlotIndex());

        }
    }
}

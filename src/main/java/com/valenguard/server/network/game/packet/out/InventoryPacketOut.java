package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.network.game.shared.Opcodes;

public class InventoryPacketOut extends AbstractServerOutPacket {

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
        } else if (inventoryAction.getInventoryActionType() == InventoryActions.SET) {
            write.writeByte(inventoryAction.getSlotIndex());
            write.writeInt(inventoryAction.getItemStack().getItemId());
            write.writeInt(inventoryAction.getItemStack().getAmount());
        } else if (inventoryAction.getInventoryActionType() == InventoryActions.MOVE) {
            write.writeByte(inventoryAction.getFromPosition());
            write.writeByte(inventoryAction.getToPosition());

            // Combining the windows into a single byte.
            byte windowsBytes = (byte) ((inventoryAction.getFromWindow() << 4) | inventoryAction.getToWindow());
            write.writeByte(windowsBytes);
        }
    }
}

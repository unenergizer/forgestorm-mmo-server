package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.inventory.InventoryActions;
import com.forgestorm.server.network.game.shared.Opcodes;

public class InventoryPacketOut extends AbstractServerOutPacket {

    private final InventoryActions inventoryActions;

    public InventoryPacketOut(final Player player, final InventoryActions inventoryActions) {
        super(Opcodes.INVENTORY_UPDATE, player.getClientHandler());
        this.inventoryActions = inventoryActions;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(inventoryActions.getInventoryActionType().getGetActionType());

        switch (inventoryActions.getInventoryActionType()) {

            case MOVE:
                write.writeByte(inventoryActions.getFromPosition());
                write.writeByte(inventoryActions.getToPosition());
                // Combining the windows into a single byte.
                byte windowsBytes = (byte) ((inventoryActions.getFromWindow() << 4) | inventoryActions.getToWindow());
                write.writeByte(windowsBytes);
                break;
            case DROP:
                break;
            case USE:
                break;
            case REMOVE:
                write.writeByte(inventoryActions.getInteractInventory());
                write.writeByte(inventoryActions.getSlotIndex());
                break;
            case SET:
                write.writeByte(inventoryActions.getInteractInventory());
                write.writeByte(inventoryActions.getSlotIndex());
                write.writeInt(inventoryActions.getItemStack().getItemId());
                write.writeInt(inventoryActions.getItemStack().getAmount());
                break;
        }
    }
}
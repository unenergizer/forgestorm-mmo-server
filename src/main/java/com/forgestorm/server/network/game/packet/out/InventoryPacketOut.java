package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.inventory.InventoryActions;
import com.forgestorm.server.network.game.shared.Opcodes;

public class InventoryPacketOut extends AbstractServerOutPacket {

    private final InventoryActions.ActionType inventoryActionType;
    private final int itemStackId;
    private final int itemStackAmount;
    private final byte slotIndex;
    private final byte fromPosition;
    private final byte toPosition;
    private final byte fromWindow;
    private final byte toWindow;
    private final byte interactInventory;

    public InventoryPacketOut(final Player player, final InventoryActions inventoryActions) {
        super(Opcodes.INVENTORY_UPDATE, player.getClientHandler());

        this.inventoryActionType = inventoryActions.getInventoryActionType();
        this.itemStackId = inventoryActions.getItemStack().getItemId();
        this.itemStackAmount = inventoryActions.getItemStack().getAmount();
        this.slotIndex = inventoryActions.getSlotIndex();
        this.fromPosition = inventoryActions.getFromPosition();
        this.toPosition = inventoryActions.getToPosition();
        this.fromWindow = inventoryActions.getFromWindow();
        this.toWindow = inventoryActions.getToWindow();
        this.interactInventory = inventoryActions.getInteractInventory();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(inventoryActionType.getGetActionType());

        switch (inventoryActionType) {
            case MOVE:
                write.writeByte(fromPosition);
                write.writeByte(toPosition);
                // Combining the windows into a single byte.
                byte windowsBytes = (byte) ((fromWindow << 4) | toWindow);
                write.writeByte(windowsBytes);
                break;
            case DROP:
            case USE:
                break;
            case REMOVE:
                write.writeByte(interactInventory);
                write.writeByte(slotIndex);
                break;
            case SET:
                write.writeByte(interactInventory);
                write.writeByte(slotIndex);
                write.writeInt(itemStackId);
                write.writeInt(itemStackAmount);
                break;
        }
    }
}

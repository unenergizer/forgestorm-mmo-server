package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.inventory.InventoryActions;
import com.forgestorm.server.network.game.shared.Opcodes;

public class InventoryPacketOut extends AbstractServerOutPacket {

    private final InventoryActions.ActionType inventoryActionType;
    private final byte slotIndex;
    private final byte fromPosition;
    private final byte toPosition;
    private final byte fromWindow;
    private final byte toWindow;
    private final byte interactInventory;

    private int itemStackId;
    private int itemStackAmount;

    public InventoryPacketOut(final Player player, final InventoryActions inventoryActions) {
        super(Opcodes.INVENTORY_UPDATE, player.getClientHandler());

        this.inventoryActionType = inventoryActions.getInventoryActionType();
        this.slotIndex = inventoryActions.getSlotIndex();
        this.fromPosition = inventoryActions.getFromPosition();
        this.toPosition = inventoryActions.getToPosition();
        this.fromWindow = inventoryActions.getFromWindow();
        this.toWindow = inventoryActions.getToWindow();
        this.interactInventory = inventoryActions.getInteractInventory();

        ItemStack itemStack = inventoryActions.getItemStack();
        if (itemStack == null) return;
        this.itemStackId = itemStack.getItemId();
        this.itemStackAmount = itemStack.getAmount();
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

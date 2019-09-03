package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.inventory.InventorySlot;
import com.valenguard.server.network.game.shared.Opcodes;

public class InspectPlayerPacketOut extends AbstractServerOutPacket {

    private final Player targetPlayer;

    public InspectPlayerPacketOut(final Player player, final Player targetPlayer) {
        super(Opcodes.INSPECT_PLAYER, player.getClientHandler());
        this.targetPlayer = targetPlayer;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        InventorySlot[] inventorySlotArray = targetPlayer.getPlayerEquipment().getInventorySlotArray();
        for (InventorySlot inventorySlot : inventorySlotArray) {
            ItemStack itemStack = inventorySlot.getItemStack();

            if (itemStack != null) {
                write.writeInt(itemStack.getItemId());
            } else {
                write.writeInt(-1);
            }
        }
    }
}

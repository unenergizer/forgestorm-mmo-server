package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.inventory.InventorySlot;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class InspectPlayerPacketOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final Player targetPlayer;

    public InspectPlayerPacketOut(final Player player, final Player targetPlayer) {
        super(Opcodes.INSPECT_PLAYER, player.getClientHandler());
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        // TODO: Remove object reference from createPacket method
        InventorySlot[] inventorySlotArray = targetPlayer.getPlayerEquipment().getInventorySlotArray();
        for (InventorySlot inventorySlot : inventorySlotArray) {
            ItemStack itemStack = inventorySlot.getItemStack();

            if (itemStack != null) {
                write.writeInt(itemStack.getItemId());
            } else {
                write.writeInt(-1);
            }
        }

        println(getClass(), "Sending inspection packet!", false, PRINT_DEBUG);
    }
}

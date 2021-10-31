package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.inventory.BankActions;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class BankManagePacketOutOut extends AbstractPacketOut {

    private final BankActions bankAction;

    public BankManagePacketOutOut(final Player player, BankActions bankAction) {
        super(Opcodes.BANK_MANAGEMENT, player.getClientHandler());

        this.bankAction = bankAction;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(bankAction.getTypeByte());
    }
}

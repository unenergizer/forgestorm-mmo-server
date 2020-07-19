package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.inventory.BankActions;
import com.forgestorm.server.network.game.shared.Opcodes;

public class BankManagePacketOut extends AbstractServerOutPacket {

    private final BankActions bankAction;

    public BankManagePacketOut(final Player player, BankActions bankAction) {
        super(Opcodes.BANK_MANAGEMENT, player.getClientHandler());

        this.bankAction = bankAction;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(bankAction.getTypeByte());
    }
}

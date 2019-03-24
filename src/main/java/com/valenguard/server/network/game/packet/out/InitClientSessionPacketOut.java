package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitClientSessionPacketOut extends AbstractServerOutPacket {

    private final boolean loginSuccess;
    private final short clientPlayerId;

    public InitClientSessionPacketOut(Player player, boolean loginSuccess, short clientPlayerId) {
        super(Opcodes.INIT_CLIENT_SESSION, player);
        this.loginSuccess = loginSuccess;
        this.clientPlayerId = clientPlayerId;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeBoolean(loginSuccess);
        write.writeShort(clientPlayerId);
    }
}

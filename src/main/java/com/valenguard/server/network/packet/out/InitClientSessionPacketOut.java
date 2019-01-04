package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class InitClientSessionPacketOut extends ServerAbstractOutPacket {

    private final boolean loginSuccess;
    private final short clientPlayerId;

    public InitClientSessionPacketOut(Player player, boolean loginSuccess, short clientPlayerId) {
        super(Opcodes.INIT_CLIENT_SESSION, player);
        this.loginSuccess = loginSuccess;
        this.clientPlayerId = clientPlayerId;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeBoolean(loginSuccess);
        write.writeShort(clientPlayerId);
    }
}

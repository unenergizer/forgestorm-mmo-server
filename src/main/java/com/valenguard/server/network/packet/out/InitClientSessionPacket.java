package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class InitClientSessionPacket extends ServerOutPacket {

    private final boolean loginSuccess;
    private final short clientPlayerId;

    public InitClientSessionPacket(Player player, boolean loginSuccess, short clientPlayerId) {
        super(Opcodes.INIT_CLIENT_SESSION, player);
        this.loginSuccess = loginSuccess;
        this.clientPlayerId = clientPlayerId;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeBoolean(loginSuccess);
        write.writeShort(clientPlayerId);
    }
}

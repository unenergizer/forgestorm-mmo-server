package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.server.network.game.shared.Opcodes;

public class PingPacketOut extends AbstractServerOutPacket {

    public PingPacketOut(final ClientHandler clientHandler) {
        super(Opcodes.PING, clientHandler);
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        clientHandler.setPingSendTime(System.currentTimeMillis());
    }
}

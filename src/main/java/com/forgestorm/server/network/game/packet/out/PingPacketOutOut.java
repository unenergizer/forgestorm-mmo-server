package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class PingPacketOutOut extends AbstractPacketOut {

    public PingPacketOutOut(final ClientHandler clientHandler) {
        super(Opcodes.PING, clientHandler);
    }

    @Override
    public void createPacket(GameOutputStream write) {
        clientHandler.setPingSendTime(System.currentTimeMillis());
    }
}

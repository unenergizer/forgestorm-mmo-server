package com.valenguard.server.network.packet.in;

import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.Opcode;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.network.shared.PacketListener;

public class PingIn implements PacketListener {

    @Opcode(getOpcode = Opcodes.PING)
    public void onPingIn(ClientHandler clientHandler) {
        long timeTaken = System.currentTimeMillis() - clientHandler.getPlayer().getPingOutTime();
        clientHandler.getPlayer().setLastPingTime(timeTaken);
    }
}

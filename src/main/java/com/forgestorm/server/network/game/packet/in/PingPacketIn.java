package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.network.game.packet.out.PingPacketOut;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.PING)
public class PingPacketIn implements PacketListener<PingPacketIn.PingPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new PingPacket(System.currentTimeMillis());
    }

    @Override
    public boolean sanitizePacket(PingPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(PingPacket packetData) {
        ClientHandler clientHandler = packetData.getClientHandler();
        long ping = packetData.packetReceivedTime - clientHandler.getPingSendTime();
        clientHandler.setClientPing(ping);
//        println(getClass(), "Account: " + clientHandler.getAuthenticatedUser().getXfAccountName() + ", Ping: " + ping);
        new PingPacketOut(clientHandler).sendPacket();
    }

    @AllArgsConstructor
    class PingPacket extends PacketData {
        private long packetReceivedTime;
    }
}
package com.valenguard.server.network.packet.in;

import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.PING)
public class PingIn implements PacketListener<PingIn.PingPacket> {

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
        long timeTaken = packetData.packetReceivedTime - packetData.getPlayer().getPingOutTime();
        packetData.getPlayer().setLastPingTime(timeTaken);
    }

    @AllArgsConstructor
    class PingPacket extends PacketData {
        private long packetReceivedTime;
    }
}
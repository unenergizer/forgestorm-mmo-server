package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.*;
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
        Player player = packetData.getClientHandler().getPlayer();

        long timeTaken = packetData.packetReceivedTime - player.getPingOutTime();
        player.setLastPingTime(timeTaken);
    }

    @AllArgsConstructor
    class PingPacket extends PacketData {
        private long packetReceivedTime;
    }
}
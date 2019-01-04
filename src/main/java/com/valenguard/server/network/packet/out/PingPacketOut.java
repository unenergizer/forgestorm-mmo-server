package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

public class PingPacketOut extends ServerAbstractOutPacket {

    public PingPacketOut(Player player) {
        super(Opcodes.PING, player);
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        player.setPingOutTime(System.currentTimeMillis());
        write.writeLong(player.getLastPingTime());
    }
}

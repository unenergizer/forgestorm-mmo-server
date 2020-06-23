package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class PingPacketOut extends AbstractServerOutPacket {

    public PingPacketOut(final Player player) {
        super(Opcodes.PING, player.getClientHandler());
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        Player player = clientHandler.getPlayer();
        player.setPingOutTime(System.currentTimeMillis());
        write.writeLong(player.getLastPingTime());
    }
}

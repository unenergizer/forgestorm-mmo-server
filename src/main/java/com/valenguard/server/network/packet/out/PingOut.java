package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class PingOut extends ServerOutPacket {

    public PingOut(Player player) {
        super(Opcodes.PING, player);
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        player.setPingOutTime(System.currentTimeMillis());
        write.writeLong(player.getLastPingTime());
    }
}

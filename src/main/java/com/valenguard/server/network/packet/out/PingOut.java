package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.ObjectOutputStream;

public class PingOut extends ServerOutPacket {

    public PingOut(Player player) {
        super(Opcodes.PING, player);
    }

    @Override
    protected void createPacket(ObjectOutputStream write) {
    }
}

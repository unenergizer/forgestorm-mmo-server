package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class InitializeMapPacket extends ServerOutPacket {

    private final String mapName;

    public InitializeMapPacket(Player player, String mapName) {
        super(Opcodes.INIT_MAP, player);
        this.mapName = mapName;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeUTF(mapName);
    }
}

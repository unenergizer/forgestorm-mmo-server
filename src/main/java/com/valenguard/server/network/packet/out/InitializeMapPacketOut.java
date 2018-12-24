package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.DataOutputStream;
import java.io.IOException;

public class InitializeMapPacketOut extends ServerAbstractOutPacket {

    private final String mapName;

    public InitializeMapPacketOut(Player player, String mapName) {
        super(Opcodes.INIT_MAP, player);
        this.mapName = mapName;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeUTF(mapName);
    }
}

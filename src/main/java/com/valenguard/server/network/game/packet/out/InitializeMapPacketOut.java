package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitializeMapPacketOut extends AbstractServerOutPacket {

    private final String mapName;

    public InitializeMapPacketOut(Player player, String mapName) {
        super(Opcodes.INIT_MAP, player);
        this.mapName = mapName;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeString(mapName);
    }
}

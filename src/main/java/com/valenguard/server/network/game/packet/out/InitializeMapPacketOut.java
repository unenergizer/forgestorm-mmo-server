package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitializeMapPacketOut extends AbstractServerOutPacket {

    private final String mapName;

    public InitializeMapPacketOut(final Player player, final String mapName) {
        super(Opcodes.INIT_MAP, player);
        this.mapName = mapName;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeString(mapName);
    }
}

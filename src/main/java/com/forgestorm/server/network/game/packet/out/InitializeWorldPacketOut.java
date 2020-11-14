package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class InitializeWorldPacketOut extends AbstractServerOutPacket {

    private final String mapName;

    public InitializeWorldPacketOut(final Player player, final String mapName) {
        super(Opcodes.INIT_MAP, player.getClientHandler());

        this.mapName = mapName;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeString(mapName);
    }
}

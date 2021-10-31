package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class InitializeWorldPacketOutOut extends AbstractPacketOut {

    private final String mapName;

    public InitializeWorldPacketOutOut(final Player player, final String mapName) {
        super(Opcodes.INIT_WORLD, player.getClientHandler());

        this.mapName = mapName;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeString(mapName);
    }
}

package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.network.game.shared.Opcodes;

public class ClientMoveResyncPacketOut extends AbstractServerOutPacket {

    private final int syncX, syncY;

    public ClientMoveResyncPacketOut(Player player, Location syncLocation) {
        super(Opcodes.CLIENT_MOVE_RESYNC, player.getClientHandler());

        syncX = syncLocation.getX();
        syncY = syncLocation.getY();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeInt(syncX);
        write.writeInt(syncY);
    }
}

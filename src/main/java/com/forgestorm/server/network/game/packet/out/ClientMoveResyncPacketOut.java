package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class ClientMoveResyncPacketOut extends AbstractPacketOut {

    private final int syncX, syncY;
    public final short syncZ;

    public ClientMoveResyncPacketOut(Player player, Location syncLocation) {
        super(Opcodes.CLIENT_MOVE_RESYNC, player.getClientHandler());

        syncX = syncLocation.getX();
        syncY = syncLocation.getY();
        syncZ = syncLocation.getZ();
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeInt(syncX);
        write.writeInt(syncY);
        write.writeShort(syncZ);
    }
}

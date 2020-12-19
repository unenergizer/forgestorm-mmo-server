package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

public class TileWarpPacketOut extends AbstractServerOutPacket {

    private final int x, y;

    public TileWarpPacketOut(final Player player, int x, int y) {
        super(Opcodes.WORLD_CHUNK_WARP, player.getClientHandler());
        this.x = x;
        this.y = y;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeInt(x);
        write.writeInt(y);
    }
}

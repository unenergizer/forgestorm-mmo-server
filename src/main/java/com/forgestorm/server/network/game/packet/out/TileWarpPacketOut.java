package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.WarpLocation;
import com.forgestorm.server.network.game.shared.Opcodes;

public class TileWarpPacketOut extends AbstractServerOutPacket {

    private final boolean clearWarps;
    private final WarpLocation warpLocation;
    private final String worldName;
    private final int toX, toY;
    private final MoveDirection facingDirection;

    public TileWarpPacketOut(final Player player, boolean clearWarps, WarpLocation warpLocation, String worldName, int toX, int toY, MoveDirection facingDirection) {
        super(Opcodes.WORLD_CHUNK_WARP, player.getClientHandler());
        this.clearWarps = clearWarps;
        this.warpLocation = warpLocation;
        this.worldName = worldName;
        this.toX = toX;
        this.toY = toY;
        this.facingDirection = facingDirection;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeBoolean(clearWarps);

        write.writeShort(warpLocation.getFromX());
        write.writeShort(warpLocation.getFromY());

        write.writeString(worldName);
        write.writeInt(toX);
        write.writeInt(toY);
        write.writeByte(facingDirection.getDirectionByte());
    }
}

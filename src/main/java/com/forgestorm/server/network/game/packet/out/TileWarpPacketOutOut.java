package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.maps.MoveDirection;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class TileWarpPacketOutOut extends AbstractPacketOut {

    private final boolean clearWarps;
    private final int fromX, fromY;
    private final short fromZ;
    private final String worldName;
    private final int toX, toY;
    private final short toZ;
    private final MoveDirection facingDirection;

    public TileWarpPacketOutOut(final Player player, boolean clearWarps, int fromX, int fromY, short fromZ, String worldName, int toX, int toY, short toZ, MoveDirection facingDirection) {
        super(Opcodes.WORLD_CHUNK_WARP, player.getClientHandler());
        this.clearWarps = clearWarps;
        this.fromX = fromX;
        this.fromY = fromY;
        this.fromZ = fromZ;
        this.worldName = worldName;
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
        this.facingDirection = facingDirection;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeBoolean(clearWarps);

        write.writeInt(fromX);
        write.writeInt(fromY);
        write.writeShort(fromZ);

        write.writeString(worldName);
        write.writeInt(toX);
        write.writeInt(toY);
        write.writeShort(toZ);
        write.writeByte(facingDirection.getDirectionByte());
    }
}

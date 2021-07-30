package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.DoorManager;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class DoorInteractPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = true;

    private final DoorManager.DoorStatus doorStatus;
    private final Tile tile;

    public DoorInteractPacketOut(final Player player, DoorManager.DoorStatus doorStatus, Tile tile) {
        super(Opcodes.DOOR_INTERACT, player.getClientHandler());
        this.doorStatus = doorStatus;
        this.tile = tile;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(DoorManager.DoorStatus.getByte(doorStatus));
        write.writeInt(tile.getWorldX());
        write.writeInt(tile.getWorldY());

        println(getClass(), "DoorStatus: " + doorStatus, false, PRINT_DEBUG);
        println(getClass(), "TileX: " + tile.getWorldX(), false, PRINT_DEBUG);
        println(getClass(), "TileY: " + tile.getWorldY(), false, PRINT_DEBUG);
    }
}

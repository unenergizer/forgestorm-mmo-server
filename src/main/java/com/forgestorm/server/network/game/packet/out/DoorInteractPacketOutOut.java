package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.DoorManager;
import com.forgestorm.server.game.world.maps.tile.Tile;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class DoorInteractPacketOutOut extends AbstractPacketOut {

    private static final boolean PRINT_DEBUG = false;

    private final DoorManager.DoorStatus doorStatus;
    private final Tile tile;

    public DoorInteractPacketOutOut(final Player player, DoorManager.DoorStatus doorStatus, Tile tile) {
        super(Opcodes.DOOR_INTERACT, player.getClientHandler());
        this.doorStatus = doorStatus;
        this.tile = tile;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(DoorManager.DoorStatus.getByte(doorStatus));
        write.writeInt(tile.getWorldX());
        write.writeInt(tile.getWorldY());
        write.writeShort(tile.getWorldZ());

        println(getClass(), "DoorStatus: " + doorStatus, false, PRINT_DEBUG);
        println(getClass(), "TileX: " + tile.getWorldX(), false, PRINT_DEBUG);
        println(getClass(), "TileY: " + tile.getWorldY(), false, PRINT_DEBUG);
        println(getClass(), "TileZ: " + tile.getWorldZ(), false, PRINT_DEBUG);
    }
}

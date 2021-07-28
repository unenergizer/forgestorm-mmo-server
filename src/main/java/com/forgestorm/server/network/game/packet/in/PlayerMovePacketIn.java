package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.task.MovementUpdateTask;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.MOVE_REQUEST)
public class PlayerMovePacketIn implements PacketListener<PlayerMovePacketIn.MovePacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final int x = clientHandler.readInt();
        final int y = clientHandler.readInt();
        return new MovePacket(x, y);
    }

    @Override
    public boolean sanitizePacket(MovePacket packetData) {
        return true;
    }

    @Override
    public void onEvent(MovePacket packetData) {
        println(getClass(), "- - //----------- PLAYER MOVE INCOMING -----------// - -", false, PRINT_DEBUG);
        Player player = packetData.getClientHandler().getPlayer();

        MovementUpdateTask movementUpdateTask = ServerMain.getInstance().getGameLoop().getMovementUpdateTask();
        Location location = new Location(player.getWorldName(), packetData.x, packetData.y);

        if (!movementUpdateTask.preMovementChecks(player, location)) {
            println(getClass(), "Player failed pre-movement checks.", false, PRINT_DEBUG);
            return;
        }

        println(getClass(), "Moving player to " + location.toString(), false, PRINT_DEBUG);
        movementUpdateTask.performPlayerMove(player, location);
    }

    @AllArgsConstructor
    static class MovePacket extends PacketData {
        int x;
        int y;
    }
}

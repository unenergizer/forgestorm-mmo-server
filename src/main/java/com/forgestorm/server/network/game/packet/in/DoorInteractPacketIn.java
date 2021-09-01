package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.maps.DoorManager;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.DOOR_INTERACT)
public class DoorInteractPacketIn implements PacketListener<DoorInteractPacketIn.DoorStatusPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        DoorManager.DoorStatus doorStatus = DoorManager.DoorStatus.getDoorStatus(clientHandler.readByte());
        int tileX = clientHandler.readInt();
        int tileY = clientHandler.readInt();
        short worldZ = clientHandler.readShort();

        println(getClass(), "DoorStatus: " + doorStatus.name(), false, PRINT_DEBUG);
        println(getClass(), "tileX: " + tileX, false, PRINT_DEBUG);
        println(getClass(), "tileY: " + tileY, false, PRINT_DEBUG);
        println(getClass(), "tileZ: " + worldZ, false, PRINT_DEBUG);

        return new DoorStatusPacket(doorStatus, tileX, tileY, worldZ);
    }

    @Override
    public boolean sanitizePacket(DoorStatusPacket packetData) {
        // Sanitize later in the DoorManager
        return true;
    }

    @Override
    public void onEvent(DoorStatusPacket packetData) {
        // Open the fucking door
        ServerMain.getInstance().getDoorManager().playerToggleDoor(
                packetData.getClientHandler().getPlayer(),
                packetData.doorStatus,
                packetData.tileX,
                packetData.tileY,
                packetData.worldZ
        );
    }

    @AllArgsConstructor
    static class DoorStatusPacket extends PacketData {
        private final DoorManager.DoorStatus doorStatus;
        private final int tileX, tileY;
        private final short worldZ;
    }
}
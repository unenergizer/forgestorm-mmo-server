package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.maps.*;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.WORLD_CHUNK_WARP)
public class TileWarpPacketIn implements PacketListener<TileWarpPacketIn.TileWarpPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        int fromX = clientHandler.readInt();
        int fromY = clientHandler.readInt();
        short fromZ = clientHandler.readShort();
        String toWorldName = clientHandler.readString();
        int toX = clientHandler.readInt();
        int toY = clientHandler.readInt();
        short toZ = clientHandler.readShort();
        byte moveDirection = clientHandler.readByte();
        return new TileWarpPacket(fromX, fromY, fromZ, toWorldName, toX, toY, toZ, MoveDirection.getDirection(moveDirection));
    }

    @Override
    public boolean sanitizePacket(TileWarpPacket packetData) {
        AuthenticatedUser authenticatedUser = packetData.getClientHandler().getAuthenticatedUser();
        boolean isAllowed = authenticatedUser.isAdmin() || authenticatedUser.isContentDeveloper();

        if (!isAllowed) {
            println(getClass(), "Non admin player attempted to create a WARP! XF Account name: " + authenticatedUser.getXfAccountName(), true);
        }

        return isAllowed;
    }

    @Override
    public void onEvent(TileWarpPacket packetData) {
        GameWorld gameWorld = packetData.getClientHandler().getPlayer().getCurrentWorldLocation().getGameWorld();
        WorldChunk worldChunk = gameWorld.findChunk(packetData.fromX, packetData.fromY);

        Location warpLocation = new Location(packetData.toWorldName, packetData.toX, packetData.toY, packetData.toZ);
        short localX = (short) (packetData.fromX - worldChunk.getChunkX() * GameConstants.CHUNK_SIZE);
        short localY = (short) (packetData.fromY - worldChunk.getChunkY() * GameConstants.CHUNK_SIZE);
        worldChunk.addTileWarp(localX, localY, packetData.fromZ, new Warp(warpLocation, packetData.facingDirection, localX, localY, packetData.fromZ));

        println(getClass(), "fromX: " + packetData.fromX, false, PRINT_DEBUG);
        println(getClass(), "fromY: " + packetData.fromY, false, PRINT_DEBUG);
        println(getClass(), "fromZ: " + packetData.fromZ, false, PRINT_DEBUG);
        println(getClass(), "toWorldName: " + packetData.toWorldName, false, PRINT_DEBUG);
        println(getClass(), "toX: " + packetData.toX, false, PRINT_DEBUG);
        println(getClass(), "toY: " + packetData.toY, false, PRINT_DEBUG);
        println(getClass(), "toZ: " + packetData.toZ, false, PRINT_DEBUG);
        println(getClass(), "MoveDirection: " + packetData.facingDirection, false, PRINT_DEBUG);
    }

    @AllArgsConstructor
    static class TileWarpPacket extends PacketData {
        private final int fromX, fromY;
        private final short fromZ;
        private final String toWorldName;
        private final int toX, toY;
        private final short toZ;
        private final MoveDirection facingDirection;
    }
}
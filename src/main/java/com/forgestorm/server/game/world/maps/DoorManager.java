package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.DoorProperty;
import com.forgestorm.shared.game.world.tile.properties.TilePropertyTypes;
import com.forgestorm.server.network.game.packet.out.DoorInteractPacketOutOut;
import com.forgestorm.server.util.ServerTimeUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DoorManager {

    private static final int MAX_TIME_DOOR_OPEN = ServerTimeUtil.getMinutes(1);

    @Getter
    private final List<DoorInfo> doorOpenList = new ArrayList<>();

    public void playerToggleDoor(Player player, DoorStatus doorStatus, int tileX, int tileY, short tileZ) {
        GameWorld gameWorld = player.getGameWorld();
        Location playerLocation = player.getCurrentWorldLocation();

        Tile tile = gameWorld.getTile(LayerDefinition.WORLD_OBJECTS, tileX, tileY, tileZ);
        TileImage tileImage = tile.getTileImage();

        if (tileImage == null) return;
        if (!tileImage.containsProperty(TilePropertyTypes.DOOR)) return;
        if (isTooFarAway(playerLocation, tileX, tileY)) return;

        DoorProperty doorProperty = (DoorProperty) tileImage.getProperty(TilePropertyTypes.DOOR);
        DoorStatus serverDoorStatus = doorProperty.getDoorStatus();

        // If the door status are the same, then do not continue
        if (serverDoorStatus == doorStatus) return;
        doorProperty.setDoorStatus(doorStatus);

        // Do door status specific operations
        switch (doorStatus) {
            case OPEN:
                doorOpenList.add(new DoorInfo(gameWorld, tile));
                break;
            case CLOSED:
                doorOpenList.removeIf(doorInfo -> doorInfo.tile == tile);
                break;
        }

        // Send network packet
        ServerMain.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                new DoorInteractPacketOutOut(player, doorStatus, tile).sendPacket());
    }

    private boolean isTooFarAway(Location playerClientLocation, int x1, int y1) {
        int x2 = playerClientLocation.getX();
        int y2 = playerClientLocation.getY();

        double distance = Math.abs(x2 - x1) + Math.abs(y2 - y1);

        return distance > GameConstants.MAX_INTERACT_DISTANCE;
    }

    public void serverForceCloseDoor(Iterator<DoorInfo> iterator, DoorInfo doorInfo) {
        DoorStatus forceCloseStatus = DoorStatus.CLOSED;
        TileImage tileImage = doorInfo.getTile().getTileImage();
        DoorProperty doorProperty = (DoorProperty) tileImage.getProperty(TilePropertyTypes.DOOR);

        // If the door is already closed, no need to continue..
        if (doorProperty.getDoorStatus() == forceCloseStatus) return;

        // Set the force close status to the door
        doorProperty.setDoorStatus(forceCloseStatus);

        // Let the players know!
        ServerMain.getInstance().getGameManager().forAllPlayers(anyPlayer ->
                new DoorInteractPacketOutOut(anyPlayer, forceCloseStatus, doorInfo.getTile()).sendPacket()
        );
        iterator.remove();
    }

    public boolean isDoorwayTraversable(Tile tile) {
        TileImage tileImage = tile.getTileImage();
        if (tileImage == null) return true;
        if (!tileImage.containsProperty(TilePropertyTypes.DOOR)) return true;

        DoorProperty doorProperty = (DoorProperty) tileImage.getProperty(TilePropertyTypes.DOOR);
        return doorProperty.getDoorStatus() == DoorStatus.OPEN;
    }

    public enum DoorStatus {
        OPEN,
        CLOSED,
        LOCKED;

        public static DoorStatus getDoorStatus(byte enumIndex) {
            for (DoorStatus doorStatus : DoorStatus.values()) {
                if ((byte) doorStatus.ordinal() == enumIndex) return doorStatus;
            }
            throw new RuntimeException("DoorStatus type miss match! Byte Received: " + enumIndex);
        }

        public static byte getByte(DoorStatus doorStatus) {
            return (byte) doorStatus.ordinal();
        }
    }

    @Getter
    public static class DoorInfo {
        private final GameWorld gameWorld;
        private final Tile tile;

        @Setter
        private int timeLeftTillAutoClose = MAX_TIME_DOOR_OPEN;

        public DoorInfo(GameWorld gameWorld, Tile tile) {
            this.gameWorld = gameWorld;
            this.tile = tile;
        }
    }
}

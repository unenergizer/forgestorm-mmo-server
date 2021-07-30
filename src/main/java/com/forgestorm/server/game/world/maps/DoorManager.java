package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.network.game.packet.out.DoorInteractPacketOut;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class DoorManager {

    private static final boolean PRINT_DEBUG = true;
    private static final int MAX_TIME_DOOR_OPEN = 120;

    @Getter
    private final List<DoorInfo> doorOpenList = new ArrayList<>();

    public void openDoor(Player player, GameWorld gameWorld, Tile tile) {

        // TODO: In the future check to see what the status of the door is. If it is locked etc..

        println(getClass(), "Door has been opened. Player: " + player.getName() + ", TileXY: " + tile.getWorldX() + "/" + tile.getWorldY());
        sendDoorStatus(DoorStatus.OPEN, player, tile);
        doorOpenList.add(new DoorInfo(player, gameWorld, tile));
    }

    public void closeDoor(Tile tile) {
        doorOpenList.removeIf(doorInfo -> {
            sendDoorStatus(DoorStatus.CLOSED, doorInfo.doorOpener, doorInfo.getTile());
            return doorInfo.tile == tile;
        });
    }

    public void forceCloseDoor(Iterator<DoorInfo> iterator, DoorInfo doorInfo) {
        println(getClass(), "Force close door. TileXY: " + doorInfo.tile.getWorldX() + "/" + doorInfo.tile.getWorldY());
        sendDoorStatus(DoorStatus.CLOSED, doorInfo.doorOpener, doorInfo.getTile());
        iterator.remove();
    }

    public void sendDoorStatus(DoorStatus doorStatus, Player player, Tile tile) {
        switch (doorStatus) {
            case OPEN:
                ServerMain.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                        new DoorInteractPacketOut(player, DoorStatus.OPEN, tile).sendPacket());
                break;
            case CLOSED:
                ServerMain.getInstance().getGameManager().sendToAll(player, clientHandler ->
                        new DoorInteractPacketOut(clientHandler.getPlayer(), DoorStatus.CLOSED, tile).sendPacket());
                break;
            default:
                new DoorInteractPacketOut(player, DoorStatus.LOCKED, tile).sendPacket();
                break;
        }
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
        private final Player doorOpener;
        private final GameWorld gameWorld;
        private final Tile tile;

        @Setter
        private int timeLeftTillAutoClose = MAX_TIME_DOOR_OPEN;

        public DoorInfo(Player doorOpener, GameWorld gameWorld, Tile tile) {
            this.doorOpener = doorOpener;
            this.gameWorld = gameWorld;
            this.tile = tile;
        }
    }
}

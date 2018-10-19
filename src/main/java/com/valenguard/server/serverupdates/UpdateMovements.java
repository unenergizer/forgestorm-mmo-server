package com.valenguard.server.serverupdates;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.Player;
import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Tile;
import com.valenguard.server.maps.data.Warp;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class UpdateMovements {

    private Queue<Player> movingPlayers = new ConcurrentLinkedDeque<>();

    private boolean hasStarted = false;
    private long startTime = 0;

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {

        if (movingPlayers.isEmpty()) return;

        // Loop through all players and process movement.
        movingPlayers.forEach(player -> {
            if (!hasStarted) {
                startTime = System.currentTimeMillis();
                hasStarted = true;
            }

            // Process the players movement.
            if (player.getCountDownMovementTicks() == 0) {

                long endTime = System.currentTimeMillis();
                System.out.println("TIME FOR 20 TICKS: " + (endTime - startTime));
                hasStarted = false;

                // If the entity has moved the required number of ticks, remove them.
                removePlayer(player);
            } else {

                //System.out.println("SPAM: Processing player movement");
                // Process number of ticks left to move
                player.processMovement();
            }
        });
    }

    /**
     * Adds a entity to the list of entities that need to be processed.
     *
     * @param player The entity ot add.
     */
    public void addPlayer(Player player, String mapName, int x, int y) {

        // TODO: REMOVE ALL THIS GARBAGE


        // Prevent the entity from being able to move while their movement is still processing.
        if (player.isMoving()) return;

        Location currentLocation = player.getLocation();
        Location futureLocation = new Location(mapName, currentLocation.getX() + x, currentLocation.getY() + y);

        System.out.println("Player trying to move to-> X: " + futureLocation.getX() + ", Y: " + futureLocation.getY());

        // Check for out of bounds.
        if (futureLocation.isOutOfBounds()) {
            System.out.println("Player attempted to move out of bounds.");

            // Grabbing the tile the player is currently standing on
            TmxMap tmxMap = player.getMapData();
            Tile currentTile = tmxMap.getTileByLocation(currentLocation);

            // TODO: PERFORM THIS AFTER THE PLAYER HAS FINISHED MOVING TOWARD THE EXIT OF
            // TODO: THE MAP

            // Check to see if the player needs to switch maps. Otherwise send a reply indicating
            // to the player that they cannot move
            if (currentTile.getWarp() != null) {
                Warp warpData = currentTile.getWarp();
                int warpX = warpData.getX();
                int warpY = warpData.getY();
                int newMapX = -1;
                int newMapY = -1;
                if (warpX != -1 && warpY != -1) {
                    // Set the location to teleport to, to be equal
                    // to the warp location
                    newMapX = warpX;
                    newMapY = warpY;
                } else {
                    // Determine the map location to teleport to by
                    // the direction the player is moving and by their
                    // current location
                    if (x == +1) newMapX = 0;                          // Left side of map
                    if (x == -1) newMapX = tmxMap.getMapWidth() - 1;  // Right side of map
                    if (x == +0) newMapX = currentLocation.getX();     // Current position X
                    if (y == +1) newMapY = 0;                          // Bottom of map
                    if (y == -1) newMapY = tmxMap.getMapHeight() - 1; // Top of map
                    if (y == +0) newMapY = currentLocation.getY();     // Current position Y
                }

                // Tell the client to switch maps
                Location teleportLocation = new Location(warpData.getMapName(), newMapX, newMapY);
                PlayerManager.getInstance().playerSwitchMap(player, teleportLocation);
            } else {
                // Tell the client to not move.
               // new MoveReply(player, false, currentLocation).sendPacket();
            }

            return;
        }

        // Check for collision. If the entity is colliding, then don't continue.
        if (!futureLocation.isTraversable()) {
            System.out.println("Player attempted to walk into a wall/object.");

            // Tell the client to not move.
            //new MoveReply(player, false, currentLocation).sendPacket();
            return;
        }

        // Setup movement before the action happens.
        player.setupMovement(futureLocation);

        // Add the entity to the list of moving entities to be processed.
        movingPlayers.add(player);

        // Send all players on this map that this player has moved.
        System.out.println("Sending all players a movement update.");
        ValenguardMain.getInstance().getMapManager().sendAllMapPlayersEntityMoveUpdate(player);

        // Tell the client to move to the requested tile.
        //new MoveReply(player, true, futureLocation).sendPacket();
    }

    /**
     * Removes a entity from the list of entities that need to be processed.
     * Then resets their movement and sends an update to all players of their
     * current location.
     *
     * @param player The entity to remove.
     */
    private void removePlayer(Player player) {
        // Remove from list so entity will not be updated.
        movingPlayers.remove(player);

        // Reset defaults
        player.resetMovement();
    }
}

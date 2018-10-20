package com.valenguard.server.serverupdates;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.Direction;
import com.valenguard.server.entity.Player;
import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Tile;
import com.valenguard.server.maps.data.Warp;
import com.valenguard.server.network.shared.ServerConstants;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class UpdateMovements {


    // todo : this number relates to how fast the client moves
    // todo: this should be read in from the server so we will change this at a later date
    private static final float MAX_TIME = .5f;


    private class MovementInfo {
        // todo: this should contain entities not players. silly idiot. fuck you
        private Player player;
        private float walkTime = 0;
    }


    private Queue<MovementInfo> movingPlayers = new ConcurrentLinkedDeque<>();

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {

        if (movingPlayers.isEmpty()) return;

        // todo: we need to be able to remove the player if they have logged off the server
        // todo: we need to do this because the list would still contain a reference to the player
        //movingPlayers.removeIf(movementInfo -> movementInfo.player.loggedOff());

        // Loop through all players and process movement.
        movingPlayers.forEach(this::updatePlayersPosition);
    }

    private void updatePlayersPosition(MovementInfo movementInfo) {

        // todo: get a delta nigga
        movementInfo.walkTime += /*delta*/ 16.0f / 20.0f;

        int currentX = movementInfo.player.getLocation().getX();
        int currentY = movementInfo.player.getLocation().getY();

        int futureX = movementInfo.player.getFutureLocation().getX();
        int futureY = movementInfo.player.getFutureLocation().getY();

        // this clearly works ;)
        movementInfo.player.setRealX(linearInterpolate(currentX, futureX, movementInfo.walkTime / MAX_TIME) * ServerConstants.TILE_SIZE);
        movementInfo.player.setRealY(linearInterpolate(currentY, futureY, movementInfo.walkTime / MAX_TIME) * ServerConstants.TILE_SIZE);
    }

    // todo: tomorrow when andrew is not sleeping on his keyboard we will fix this.
    public float linearInterpolate(float start, float end, float a) {
        return start + (end - start) /*   * apply(a)    */;
    }

    /**
     * Adds a entity to the list of entities that need to be processed.
     *
     * @param player The entity ot add.
     */
    public void addPlayer(Player player, String mapName, Direction direction) {

        // Since they player is already moving they must be predicting movement.
        if (player.isMoving()) {
            predictMovement(player, mapName, direction);
            return;
        }

        newPlayerMove(player, mapName, direction);
    }

    private void predictMovement(Player player, String mapName, Direction direction) {
        int[] x = new int[1];
        int[] y = new int[1];
        getMovementAmounts(x, y, direction);

        // No reason to update their predicted direction since we already predict
        // this current direction.
        if (player.getPredictedDirection() == direction) return;

        // todo: map warming checks

        // The player is attempting to move somewhere in the future that the map doesn't allow.
        if (isMovable(mapName, player.getLocation().getX() + x[0], player.getLocation().getY() + y[0])) {
            return;
        }

        player.setPredictedDirection(direction);
        // todo: tell everyone except the player that sent the information
        // todo: that they plan on changing directions
    }

    private void newPlayerMove(Player player, String mapName, Direction direction) {
        int[] x = new int[1];
        int[] y = new int[1];
        getMovementAmounts(x, y, direction);

        // todo: map warming checks

        // The player is attempting to move somewhere that the map doesn't allow.
        if (isMovable(mapName, player.getLocation().getX() + x[0], player.getLocation().getY() + y[0])) {
            return;
        }

        // todo: setup new movement for the player

    }

    // todo exchange arrays for some type of object pair
    private void getMovementAmounts(int x[], int y[], Direction direction) {
        x[0] = 0;
        y[0] = 0;
        if (direction == Direction.DOWN) y[0] = -1;
        if (direction == Direction.UP) y[0] = 1;
        if (direction == Direction.LEFT) x[0] = -1;
        if (direction == Direction.RIGHT) x[0] = 1;
    }

    private boolean isMovable(String mapName, int x, int y) {

        TmxMap tmxMap = ValenguardMain.getInstance().getMapManager().getMapData(mapName);

        if (!tmxMap.isTraversable(x, y)) {
            // Play sound or something
            return false;
        }

        if (tmxMap.isOutOfBounds(x, y)) {
            // Play sound or something
            return false;
        }

        return true;
    }

    // todo: add this later if needed
//    /**
//     * Removes a entity from the list of entities that need to be processed.
//     * Then resets their movement and sends an update to all players of their
//     * current location.
//     *
//     * @param player The entity to remove.
//     */
//    private void removePlayer(Player player) {
//        // Remove from list so entity will not be updated.
//        movingPlayers.remove(player);
//
//        // Reset defaults
//        player.resetMovement();
//    }
}

package com.valenguard.server.serverupdates;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.Direction;
import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.network.packet.out.MoveEntityPacket;
import com.valenguard.server.network.shared.ServerConstants;
import lombok.AllArgsConstructor;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class UpdateMovements {


    // todo : this number relates to how fast the client moves
    // todo: this should be read in from the server so we will change this at a later date
    private static final float MAX_TIME = .5f;


    @AllArgsConstructor
    private class MovementInfo {
        // todo: this should contain entities not players. silly idiot. fuck you
        private Player player;
        private float walkTime;
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

        movementInfo.walkTime += /*delta*/ 1.0f / 20.0f;

        int currentX = movementInfo.player.getCurrentMapLocation().getX();
        int currentY = movementInfo.player.getCurrentMapLocation().getY();

        int futureX = movementInfo.player.getFutureMapLocation().getX();
        int futureY = movementInfo.player.getFutureMapLocation().getY();

        // this clearly works ;)
        movementInfo.player.setRealX(linearInterpolate(currentX, futureX, movementInfo.walkTime / MAX_TIME) * ServerConstants.TILE_SIZE);
        movementInfo.player.setRealY(linearInterpolate(currentY, futureY, movementInfo.walkTime / MAX_TIME) * ServerConstants.TILE_SIZE);

        if (movementInfo.walkTime <= MAX_TIME) return;

        if (movementInfo.player.getPredictedDirection() == Direction.STOP) {
            finishPlayerMove(movementInfo);
        } else {
            predictNextMovement(movementInfo);
        }
    }

    private void finishPlayerMove(MovementInfo movementInfo) {

        movementInfo.player.getCurrentMapLocation().set(movementInfo.player.getFutureMapLocation());

        // Clamping the player
        movementInfo.player.setRealX(movementInfo.player.getFutureMapLocation().getX() * ServerConstants.TILE_SIZE);
        movementInfo.player.setRealY(movementInfo.player.getFutureMapLocation().getY() * ServerConstants.TILE_SIZE);

        movementInfo.player.setMoveDirection(Direction.STOP);
        movementInfo.player.setPredictedDirection(Direction.STOP);
        movingPlayers.remove(movementInfo);

        // todo should we send out packets confirming that they have arrived?
    }

    private void predictNextMovement(MovementInfo movementInfo) {

        Entity entity = movementInfo.player;
        Direction predictedDirection = movementInfo.player.getPredictedDirection();

        // Change player directional information for continuing movement.
        // todo on the client this logic caused it to mess up and we needed to call .set()
        entity.getCurrentMapLocation().set(movementInfo.player.getCurrentMapLocation());
        entity.setMoveDirection(movementInfo.player.getPredictedDirection());

        if (predictedDirection == Direction.UP) entity.getFutureMapLocation().add(0, 1);
        if (predictedDirection == Direction.DOWN) entity.getFutureMapLocation().add(0, -1);
        if (predictedDirection == Direction.LEFT) entity.getFutureMapLocation().add(-1, 0);
        if (predictedDirection == Direction.RIGHT) entity.getFutureMapLocation().add(1, 0);

        int[] amountX = new int[1];
        int[] amountY = new int[1];
        getMovementAmounts(amountX, amountY, predictedDirection);

        // todo this is a terrible way to get the map name
        if (!isMovable(entity.getMapData().getMapName().replace(".tmx", ""),
                entity.getFutureMapLocation().getX() + amountX[0], entity.getFutureMapLocation().getY() + amountY[0])) {
            entity.setPredictedDirection(Direction.STOP);
        }

        movementInfo.walkTime = 0;

        // todo send packets
    }

    // todo: tomorrow when andrew is not sleeping on his keyboard we will fix this.
    public float linearInterpolate(float start, float end, float a) {
        return start + (end - start) * a;
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
        if (!isMovable(mapName, player.getFutureMapLocation().getX() + x[0], player.getFutureMapLocation().getY() + y[0])) {

            System.out.println("FOR PREDICTED MOVEMENT IT RETURNED AS NOT MOVABLE!");

            return;
        }

        player.setPredictedDirection(direction);

        // tell everyone except the player that sent the information
        // that they plan on changing directions
        // todo some method to make it where sending all but to one player would be easier
        player.getMapData().getPlayerList().forEach(playerToInform -> {
            if (playerToInform.equals(player)) return;
            new MoveEntityPacket(playerToInform, player, direction).sendPacket();
        });
    }

    private void newPlayerMove(Player player, String mapName, Direction direction) {
        int[] x = new int[1];
        int[] y = new int[1];
        getMovementAmounts(x, y, direction);

        Location futureLocation = new Location(mapName,player.getCurrentMapLocation().getX() + x[0], player.getCurrentMapLocation().getY() + y[0]);

        // todo: map warming checks

        // The player is attempting to move somewhere that the map doesn't allow.
        if (!isMovable(mapName, futureLocation.getX(), futureLocation.getY())) {
            return;
        }

        player.setMoveDirection(direction);
        player.setPredictedDirection(Direction.STOP);
        player.setFutureMapLocation(futureLocation);
        movingPlayers.add(new MovementInfo(player, 0));

        // Telling all players except the player that just moved
        // that the player is starting to move.
        // todo some method to make it where sending all but to one player would be easier
        futureLocation.getMapData().getPlayerList().forEach(playerToInform -> {
            if (playerToInform.equals(player)) return;
            new MoveEntityPacket(playerToInform, player, direction).sendPacket();
        });
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
//     * current currentMapLocation.
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

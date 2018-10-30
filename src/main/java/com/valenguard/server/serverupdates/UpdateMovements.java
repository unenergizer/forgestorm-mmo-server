package com.valenguard.server.serverupdates;

import com.valenguard.server.entity.*;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.MoveUtil;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.network.packet.out.MoveEntityPacket;
import com.valenguard.server.network.shared.ServerConstants;

public class UpdateMovements {

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {

        // Loop through all players and process movement.
        EntityManager.getInstance().getEntities().stream().filter(MoveUtil::isEntityMoving).forEach(this::updateEntitiesPosition);
    }

    private void updateEntitiesPosition(Entity entity) {

//        System.out.println("Ticking...");

        // todo: get a delta nigga
        float delta = 1.0f / 20.0f;

        entity.setWalkTime(entity.getWalkTime() + delta);

        int currentX = entity.getCurrentMapLocation().getX();
        int currentY = entity.getCurrentMapLocation().getY();

        int futureX = entity.getFutureMapLocation().getX();
        int futureY = entity.getFutureMapLocation().getY();

        // this clearly works ;)
        entity.setRealX(linearInterpolate(currentX, futureX, entity.getWalkTime() / entity.getMoveSpeed()) * ServerConstants.TILE_SIZE);
        entity.setRealY(linearInterpolate(currentY, futureY, entity.getWalkTime() / entity.getMoveSpeed()) * ServerConstants.TILE_SIZE);

        if (entity.getWalkTime() <= entity.getMoveSpeed()) return;

        System.out.println("Performing arrival code");

        if (entity instanceof Player) {

            Player player = (Player) entity;

            System.out.println("finishMove()");
            finishMove(entity);

            if (player.getLatestMoveRequest() != null) {
                System.out.println("Readding player addPlayer() for more movement");
                addPlayer(player, player.getLatestMoveRequest());
                player.setLatestMoveRequest(null);
            }

        } else { // todo figure out how to handle other entities

        }
    }

    private void finishMove(Entity entity) {
        System.out.println("PLAYER HAS ARRIVED AT [" + entity.getFutureMapLocation().getX() + ", " + entity.getFutureMapLocation().getY() + "]");
        entity.getCurrentMapLocation().set(entity.getFutureMapLocation());
        entity.setRealX(entity.getFutureMapLocation().getX() * ServerConstants.TILE_SIZE);
        entity.setRealY(entity.getFutureMapLocation().getY() * ServerConstants.TILE_SIZE);
    }

    private float linearInterpolate(float start, float end, float a) {
        return start + (end - start) * a;
    }

    /**
     * Adds a entity to the list of entities that need to be processed.
     *
     * @param player The entity ot add.
     */
    public void addPlayer(Player player, MoveDirection direction) {

        if (MoveUtil.isEntityMoving(player)) {
            // What if the player actually did arrive and the server is somehow behind?...
            System.out.println("For some dumb reason the server thinks the player is already moving");
            player.setLatestMoveRequest(direction);
            return;
        }

        Location addToLocation = MoveUtil.getLocation(player.getMapData(), direction);
        Location attemptLocation = new Location(player.getCurrentMapLocation()).add(addToLocation);

        // Prevents the player from moving places they are not allowed to go.
        if (!isMovable(player.getMapData(), attemptLocation.getX(), attemptLocation.getY())) {
            System.out.println("Is not movable");
            return;
        }

        player.setFutureMapLocation(attemptLocation);
        player.setWalkTime(0f);

        System.out.println("Future Location [" + attemptLocation.getY() + ", " + attemptLocation.getY());

        System.out.println("Sending that data to the other clients");
        PlayerManager.getInstance().sendToAllButPlayer(player, clientHandler ->
                new MoveEntityPacket(clientHandler.getPlayer(), player, direction).sendPacket());
    }

    private boolean isMovable(TmxMap tmxMap, int x, int y) {

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
//        movingEntities.remove(player);
//
//        // Reset defaults
//        player.resetMovement();
//    }
}

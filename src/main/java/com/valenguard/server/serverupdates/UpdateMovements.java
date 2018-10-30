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
        EntityManager.getInstance().getEntities().stream().filter(MoveUtil::isEntityMoving).forEach(this::updateEntitiesPosition);
    }

    private void updateEntitiesPosition(Entity entity) {
        float delta = 1.0f / 20.0f;

        entity.setWalkTime(entity.getWalkTime() + delta);

        int currentX = entity.getCurrentMapLocation().getX();
        int currentY = entity.getCurrentMapLocation().getY();

        int futureX = entity.getFutureMapLocation().getX();
        int futureY = entity.getFutureMapLocation().getY();

        entity.setRealX(linearInterpolate(currentX, futureX, entity.getWalkTime() / entity.getMoveSpeed()) * ServerConstants.TILE_SIZE);
        entity.setRealY(linearInterpolate(currentY, futureY, entity.getWalkTime() / entity.getMoveSpeed()) * ServerConstants.TILE_SIZE);

        if (entity.getWalkTime() <= entity.getMoveSpeed()) return;

        if (entity instanceof Player) {

            Player player = (Player) entity;
            finishMove(entity);

            if (player.getLatestMoveRequest() != null) {
                addPlayer(player, player.getLatestMoveRequest());
                player.setLatestMoveRequest(null);
            }
        } else {
            // todo figure out how to handle other entities
            System.err.println("[UpdateMovements] Deal with other entities");
        }
    }

    private void finishMove(Entity entity) {
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
            player.setLatestMoveRequest(direction);
            return;
        }

        Location addToLocation = MoveUtil.getLocation(player.getMapData(), direction);
        Location attemptLocation = new Location(player.getCurrentMapLocation()).add(addToLocation);

        // Prevents the player from moving places they are not allowed to go.
        if (!isMovable(player.getMapData(), attemptLocation.getX(), attemptLocation.getY())) return;

        player.setFutureMapLocation(attemptLocation);
        player.setWalkTime(0f);

        PlayerManager.getInstance().sendToAllButPlayer(player, clientHandler ->
                new MoveEntityPacket(clientHandler.getPlayer(), player, attemptLocation).sendPacket());
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
}

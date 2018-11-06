package com.valenguard.server.serverupdates;

import com.valenguard.server.entity.*;
import com.valenguard.server.maps.MapUtil;
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

            if (!player.getLatestMoveRequests().isEmpty()) {
                addPlayer(player, player.getLatestMoveRequests().remove());
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

        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        if (player.getWarp() != null) {
            PlayerManager.getInstance().playerSwitchMap(player, player.getWarp());
            player.setWarp(null);

            System.out.println("===[P WARP]========================");
            System.out.println("Map: " + player.getCurrentMapLocation().getMapName());
            System.out.println("CLx: " + player.getCurrentMapLocation().getX());
            System.out.println("CLy: " + player.getCurrentMapLocation().getY());
            System.out.println("FLx: " + player.getFutureMapLocation().getX());
            System.out.println("FLy: " + player.getFutureMapLocation().getY());
            System.out.println("DRx: " + player.getRealX());
            System.out.println("DRy: " + player.getRealY());
        }
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
        if (player.getWarp() != null) return; // Stop player moving during warp init
        if (MoveUtil.isEntityMoving(player)) {
            player.addDirectionToFutureQueue(direction);
            return;
        }

        Location addToLocation = MoveUtil.getLocation(player.getTmxMap(), direction);
        Location attemptLocation = new Location(player.getCurrentMapLocation()).add(addToLocation);

        // Prevents the player from moving places they are not allowed to go.
        if (!isMovable(player.getTmxMap(), attemptLocation.getX(), attemptLocation.getY())) return;

        if (MapUtil.locationHasWarp(attemptLocation)) {
            player.setWarp(MapUtil.getWarpFromLocation(attemptLocation));
        }

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

        return !tmxMap.isOutOfBounds(x, y);
    }
}

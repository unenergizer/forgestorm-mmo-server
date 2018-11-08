package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.packet.out.MoveEntityPacket;

public class UpdateMovements {

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {
        ValenguardMain.getInstance().getGameManager().forAllPlayers(this::updateEntitiesPosition);
    }

    private void updateEntitiesPosition(MovingEntity entity) {

        moveEntity(entity);

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

    private void moveEntity(MovingEntity entity) {
        float delta = 1.0f / 20.0f;

        entity.setWalkTime(entity.getWalkTime() + delta);

        int currentX = entity.getCurrentMapLocation().getX();
        int currentY = entity.getCurrentMapLocation().getY();

        int futureX = entity.getFutureMapLocation().getX();
        int futureY = entity.getFutureMapLocation().getY();

        entity.setRealX(linearInterpolate(currentX, futureX, entity.getWalkTime() / entity.getMoveSpeed()) * GameConstants.TILE_SIZE);
        entity.setRealY(linearInterpolate(currentY, futureY, entity.getWalkTime() / entity.getMoveSpeed()) * GameConstants.TILE_SIZE);
    }

    private void finishMove(MovingEntity entity) {

        entity.getCurrentMapLocation().set(entity.getFutureMapLocation());
        entity.setRealX(entity.getFutureMapLocation().getX() * GameConstants.TILE_SIZE);
        entity.setRealY(entity.getFutureMapLocation().getY() * GameConstants.TILE_SIZE);

        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        if (player.getWarp() != null) {
            ValenguardMain.getInstance().getGameManager().playerSwitchGameMap(player);
            player.setWarp(null);

            System.out.println("===[P WARP]========================");
            System.out.println("GameMap: " + player.getCurrentMapLocation().getMapName());
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

        if (isEntityMoving(player)) {
            player.addDirectionToFutureQueue(direction);
            return;
        }

        Location addToLocation = getLocation(player, direction);
        Location attemptLocation = new Location(player.getCurrentMapLocation()).add(addToLocation);

        // Prevents the player from moving places they are not allowed to go.
        if (!player.getGameMap().isMovable(attemptLocation)) return;

        if (player.getGameMap().locationHasWarp(attemptLocation)) {
            player.setWarp(player.getGameMap().getWarpFromLocation(attemptLocation));
        }

        player.setFutureMapLocation(attemptLocation);
        player.setWalkTime(0f);

        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                new MoveEntityPacket(clientHandler.getPlayer(), player, attemptLocation).sendPacket());
    }

    private Location getLocation(Player player, MoveDirection direction) {
        if (direction == MoveDirection.DOWN) return new Location(player.getMapName(), 0, -1);
        if (direction == MoveDirection.UP) return new Location(player.getMapName(), 0, 1);
        if (direction == MoveDirection.LEFT) return new Location(player.getMapName(), -1, 0);
        if (direction == MoveDirection.RIGHT) return new Location(player.getMapName(), 1, 0);
        return null;
    }

    private boolean isEntityMoving(MovingEntity entity) {
        return entity.getCurrentMapLocation().getX() != entity.getFutureMapLocation().getX() || entity.getCurrentMapLocation().getY() != entity.getFutureMapLocation().getY();
    }
}

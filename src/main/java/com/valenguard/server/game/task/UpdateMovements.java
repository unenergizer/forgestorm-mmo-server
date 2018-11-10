package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.packet.out.EntityMovePacket;
import com.valenguard.server.util.Log;

public class UpdateMovements {

    private final static boolean PRINT_DEBUG = false;

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {
        ValenguardMain.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);
    }

    private void updateEntitiesPosition(MovingEntity movingEntity) {

        moveEntity(movingEntity);

        if (movingEntity.getWalkTime() <= movingEntity.getMoveSpeed()) return;

        if (movingEntity instanceof Player) {

            Player player = (Player) movingEntity;
            finishMove(movingEntity);

            if (!player.getLatestMoveRequests().isEmpty()) {
                performMove(player, player.getLatestMoveRequests().remove());
            }
        } else {
            // todo figure out how to handle other entities
            Log.println(getClass(), "TODO: Deal with other entities", true);
        }
    }

    private void moveEntity(MovingEntity movingEntity) {
        float delta = 1.0f / 20.0f;

        movingEntity.setWalkTime(movingEntity.getWalkTime() + delta);

        int currentX = movingEntity.getCurrentMapLocation().getX();
        int currentY = movingEntity.getCurrentMapLocation().getY();

        int futureX = movingEntity.getFutureMapLocation().getX();
        int futureY = movingEntity.getFutureMapLocation().getY();

        movingEntity.setRealX(linearInterpolate(currentX, futureX, movingEntity.getWalkTime() / movingEntity.getMoveSpeed()) * GameConstants.TILE_SIZE);
        movingEntity.setRealY(linearInterpolate(currentY, futureY, movingEntity.getWalkTime() / movingEntity.getMoveSpeed()) * GameConstants.TILE_SIZE);
    }

    private float linearInterpolate(float start, float end, float a) {
        return start + (end - start) * a;
    }

    private void finishMove(MovingEntity movingEntity) {
        /*Log.println(getClass(),
                "\nID -> " + movingEntity.getServerEntityId() +
                        "\nMAP -> " + movingEntity.getFutureMapLocation().getMapName() +
                        "\nX -> " + movingEntity.getFutureMapLocation().getX() +
                        "\nY -> " + movingEntity.getFutureMapLocation().getY() +
                        "\nName -> " + movingEntity.getName() +
                        "\nMoveSpeed -> " + movingEntity.getMoveSpeed() +
                        "\nFaceDir -> " + movingEntity.getFacingDirection().getDirectionByte());*/
        movingEntity.getCurrentMapLocation().set(movingEntity.getFutureMapLocation());
        movingEntity.setRealX(movingEntity.getFutureMapLocation().getX() * GameConstants.TILE_SIZE);
        movingEntity.setRealY(movingEntity.getFutureMapLocation().getY() * GameConstants.TILE_SIZE);
    }

    /**
     * Adds a entity to the list of entities that need to be processed.
     *
     * @param player The entity ot add.
     */
    public void performMove(Player player, MoveDirection direction) {
        if (player.getWarp() != null) return; // Stop player moving during warp init

        if (isEntityMoving(player)) {
            player.addDirectionToFutureQueue(direction);
            return;
        }

        Location addToLocation = getAddToLocation(player, direction);
        Location attemptLocation = new Location(player.getCurrentMapLocation()).add(addToLocation);

        if (attemptLocation.equals(player.getCurrentMapLocation())) {
            Log.println(getClass(), "A player tried request a movement to the tile they are already on.", true);
            return;
        }

        // Prevents the player from moving places they are not allowed to go.
        if (!player.getGameMap().isMovable(attemptLocation)) return;

        if (player.getGameMap().locationHasWarp(attemptLocation)) {
            player.setWarp(player.getGameMap().getWarpFromLocation(attemptLocation));
        }

        player.setFutureMapLocation(attemptLocation);
        player.setWalkTime(0f);
        player.setFacingDirection(direction);

        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                new EntityMovePacket(clientHandler.getPlayer(), player, attemptLocation).sendPacket());
    }

    private Location getAddToLocation(Player player, MoveDirection direction) {
        if (direction == MoveDirection.DOWN) return new Location(player.getMapName(), 0, -1);
        if (direction == MoveDirection.UP) return new Location(player.getMapName(), 0, 1);
        if (direction == MoveDirection.LEFT) return new Location(player.getMapName(), -1, 0);
        if (direction == MoveDirection.RIGHT) return new Location(player.getMapName(), 1, 0);
        return null;
    }

    private boolean isEntityMoving(MovingEntity movingEntity) {
        return movingEntity.getCurrentMapLocation().getX() != movingEntity.getFutureMapLocation().getX()
                || movingEntity.getCurrentMapLocation().getY() != movingEntity.getFutureMapLocation().getY();
    }
}

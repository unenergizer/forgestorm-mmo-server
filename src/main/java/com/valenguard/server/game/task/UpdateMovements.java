package com.valenguard.server.game.task;

import com.google.common.base.Preconditions;
import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.AIEntity;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.packet.out.EntityMovePacketOut;
import com.valenguard.server.util.Log;

public class UpdateMovements {

    private final static boolean PRINT_DEBUG = false;

    /**
     * Process the list of moving players.
     */
    public void updatePlayerMovement() {
        ValenguardMain.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);

        // Try and start an entity move
        ValenguardMain.getInstance().getGameManager().forAllMobsFiltered(entity -> generateNewAIMovements((MovingEntity) entity),
                entity -> entity instanceof MovingEntity && !((MovingEntity) entity).isEntityMoving());

        // Continue entity movement
        ValenguardMain.getInstance().getGameManager().forAllMobsFiltered(entity -> updateEntitiesPosition((MovingEntity) entity),
                entity -> entity instanceof MovingEntity && ((MovingEntity) entity).isEntityMoving());
    }

    private void generateNewAIMovements(MovingEntity movingEntity) {
        if (movingEntity.getEntityType() != EntityType.NPC && movingEntity.getEntityType() != EntityType.MONSTER) return;
        MoveDirection moveDirection = ((AIEntity) movingEntity).getRandomRegionMoveGenerator().generateMoveDirection(false);

        // Start performing a movement if the entity is not moving
        if (moveDirection != MoveDirection.NONE) {
            Log.println(getClass(), "Npc has started moving.", false, PRINT_DEBUG);
            performMove(movingEntity, moveDirection);
        }
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

            finishMove(movingEntity);

            if (movingEntity.getEntityType() == EntityType.NPC || movingEntity.getEntityType() == EntityType.MONSTER) {

                Log.println(getClass(), "Generating a new move.", false, PRINT_DEBUG);

                ((AIEntity) movingEntity).getRandomRegionMoveGenerator().setAlreadyDeterminedMove(false);
                MoveDirection predictedMoveDirection = ((AIEntity) movingEntity).getRandomRegionMoveGenerator().generateMoveDirection(true);

                if (predictedMoveDirection != MoveDirection.NONE) {
                    performMove(movingEntity, predictedMoveDirection);
                }
            }
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
        Log.println(getClass(), "EntityId: " + movingEntity.getServerEntityId() + " has finished it's move", false, PRINT_DEBUG);
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

        Location addToLocation = player.getGameMap().getLocation(direction);
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
                new EntityMovePacketOut(clientHandler.getPlayer(), player, attemptLocation).sendPacket());
    }

    private void performMove(MovingEntity movingEntity, MoveDirection moveDirection) {

        Preconditions.checkArgument(moveDirection != MoveDirection.NONE, "The requested move direction was NONE!");

        Location futureLocation = new Location(movingEntity.getCurrentMapLocation()).add(movingEntity.getGameMap().getLocation(moveDirection));
        movingEntity.setFutureMapLocation(futureLocation);
        movingEntity.setWalkTime(0f);
        movingEntity.setFacingDirection(moveDirection);

        Log.println(getClass(), "CurrentLocation: " + movingEntity.getCurrentMapLocation(), false, PRINT_DEBUG);
        Log.println(getClass(), "FutureLocation: " + movingEntity.getFutureMapLocation(),false, PRINT_DEBUG);


        movingEntity.getGameMap().getPlayerList().forEach(player ->
                new EntityMovePacketOut(player, movingEntity, movingEntity.getFutureMapLocation()).sendPacket());
    }

    private boolean isEntityMoving(MovingEntity movingEntity) {
        return movingEntity.getCurrentMapLocation().getX() != movingEntity.getFutureMapLocation().getX()
                || movingEntity.getCurrentMapLocation().getY() != movingEntity.getFutureMapLocation().getY();
    }
}

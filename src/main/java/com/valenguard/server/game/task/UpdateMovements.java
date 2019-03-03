package com.valenguard.server.game.task;

import com.google.common.base.Preconditions;
import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.AIEntity;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.network.packet.out.EntityMovePacketOut;

import static com.valenguard.server.util.Log.println;

public class UpdateMovements {

    private final static boolean PRINT_DEBUG = false;

    /**
     * Process the list of moving entities.
     */
    public void updateEntityMovement() {
        ValenguardMain.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);

        // Try and start an entity move
        ValenguardMain.getInstance().getGameManager().forAllMobsFiltered(entity -> generateNewAIMovements((MovingEntity) entity),
                entity -> !entity.isEntityMoving());

        // Continue entity movement
        ValenguardMain.getInstance().getGameManager().forAllMobsFiltered(entity -> updateEntitiesPosition((MovingEntity) entity),
                MovingEntity::isEntityMoving);
    }

    private void generateNewAIMovements(MovingEntity movingEntity) {
        if (movingEntity.getEntityType() != EntityType.NPC && movingEntity.getEntityType() != EntityType.MONSTER)
            return;

        // We have a target to follow, so no random movements
        if (movingEntity.getTargetEntity() != null) return;

        MoveDirection moveDirection = ((AIEntity) movingEntity).getRandomRegionMoveGenerator().generateMoveDirection(false);

        // Start performing a movement if the entity is not moving
        if (moveDirection != MoveDirection.NONE) {
            println(getClass(), "NPC has started moving.", false, PRINT_DEBUG);
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

                println(getClass(), "Generating a new move.", false, PRINT_DEBUG);

                ((AIEntity) movingEntity).getRandomRegionMoveGenerator().setAlreadyDeterminedMove(false);

                if (movingEntity.getTargetEntity() != null) return;

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
        println(getClass(), "EntityId: " + movingEntity.getServerEntityId() + " has finished it's move", false, PRINT_DEBUG);

        movingEntity.getCurrentMapLocation().set(movingEntity.getFutureMapLocation());
        movingEntity.setRealX(movingEntity.getFutureMapLocation().getX() * GameConstants.TILE_SIZE);
        movingEntity.setRealY(movingEntity.getFutureMapLocation().getY() * GameConstants.TILE_SIZE);

        initEntityTargeting(movingEntity);
    }

    private void initEntityTargeting(MovingEntity movingEntity) {
        GameMap gameMap = movingEntity.getGameMap();

        // Find MovingEntity targets
        for (MovingEntity otherMovingMobs : gameMap.getAiEntityMap().values()) {
            if (movingEntity.equals(otherMovingMobs)) continue;
            if (movingEntity.getEntityType() == otherMovingMobs.getEntityType()) continue;

            findEntityTarget(movingEntity, otherMovingMobs);
        }

        if (movingEntity instanceof Player) {
            for (MovingEntity otherMovingMobs : gameMap.getAiEntityMap().values()) {
                findEntityTarget(otherMovingMobs, movingEntity);
            }
        }

        // Find player targets
        for (Player player : gameMap.getPlayerList()) {
            if (movingEntity.equals(player)) continue;
            findEntityTarget(movingEntity, player);
        }
    }

    private void findEntityTarget(MovingEntity movingEntity, MovingEntity targetEntity) {
        Location currentLocation = targetEntity.getCurrentMapLocation();
        Location targetLocation = movingEntity.getCurrentMapLocation();

        // This entity has no target, find one?
        if (movingEntity.getTargetEntity() == null) {

            // Is player within distance to target
            if (currentLocation.isWithinDistance(targetLocation, GameConstants.START_ATTACK_RADIUS)) {
                if (movingEntity.getEntityAlignment() == EntityAlignment.HOSTILE) {
                    movingEntity.setTargetEntity(targetEntity);
                    findTrackingPath(movingEntity, targetEntity);
                }
            }
        } else if (movingEntity.getTargetEntity().equals(targetEntity)) {
            // We already have a target, so lets attack
            if (currentLocation.isWithinDistance(targetLocation, GameConstants.QUIT_ATTACK_RADIUS)) {
                findTrackingPath(movingEntity, targetEntity);
            } else {
                movingEntity.setTargetEntity(null);
            }
        }
    }

    private void findTrackingPath(MovingEntity movingEntity, MovingEntity targetPlayer) {
        if (movingEntity.isEntityMoving()) return;
        if (movingEntity instanceof Player) return; // Do not generate AI movements for players

        GameMap gameMap = movingEntity.getGameMap();
        Location currentLocation = movingEntity.getCurrentMapLocation();
        Location targetLocation = targetPlayer.getCurrentMapLocation();

        movingEntity.setTargetEntity(targetPlayer);

        Location northLocation = currentLocation.add(gameMap.getLocation(MoveDirection.NORTH));
        Location southLocation = currentLocation.add(gameMap.getLocation(MoveDirection.SOUTH));
        Location eastLocation = currentLocation.add(gameMap.getLocation(MoveDirection.EAST));
        Location westLocation = currentLocation.add(gameMap.getLocation(MoveDirection.WEST));

        if (targetLocation.getX() > currentLocation.getX()) {
            if (!(currentLocation.getX() + 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(eastLocation))
                    performMove(movingEntity, MoveDirection.EAST);
            }
        } else if (targetLocation.getX() < currentLocation.getX()) {
            if (!(currentLocation.getX() - 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(westLocation))
                    performMove(movingEntity, MoveDirection.WEST);
            }
        } else if (targetLocation.getY() > currentLocation.getY()) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() + 1 == targetLocation.getY())) {
                if (gameMap.isMovable(northLocation))
                    performMove(movingEntity, MoveDirection.NORTH);
            }
        } else if (targetLocation.getY() < currentLocation.getY()) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() - 1 == targetLocation.getY())) {
                if (gameMap.isMovable(southLocation))
                    performMove(movingEntity, MoveDirection.SOUTH);
            }
        } else {
            // on top of each other
            if (gameMap.isMovable(northLocation)) {
                performMove(movingEntity, MoveDirection.NORTH);
            } else if (gameMap.isMovable(southLocation)) {
                performMove(movingEntity, MoveDirection.SOUTH);
            } else if (gameMap.isMovable(westLocation)) {
                performMove(movingEntity, MoveDirection.WEST);
            } else if (gameMap.isMovable(eastLocation)) {
                performMove(movingEntity, MoveDirection.EAST);
            } else {
                println(getClass(), "Setting target null??");
                movingEntity.setTargetEntity(null);
            }
        }
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
            println(getClass(), "A player tried request a movement to the tile they are already on.", true);
            return;
        }

        // Prevents the player from moving places they are not allowed to go.
        if (!player.getGameMap().isMovable(attemptLocation)) return;

        // Canceling trade for the player.
        ValenguardMain.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Players can not move when trading.");

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

        println(getClass(), "CurrentLocation: " + movingEntity.getCurrentMapLocation(), false, PRINT_DEBUG);
        println(getClass(), "FutureLocation: " + movingEntity.getFutureMapLocation(), false, PRINT_DEBUG);

        movingEntity.getGameMap().getPlayerList().forEach(player ->
                new EntityMovePacketOut(player, movingEntity, movingEntity.getFutureMapLocation()).sendPacket());
    }

    private boolean isEntityMoving(MovingEntity movingEntity) {
        return movingEntity.getCurrentMapLocation().getX() != movingEntity.getFutureMapLocation().getX()
                || movingEntity.getCurrentMapLocation().getY() != movingEntity.getFutureMapLocation().getY();
    }
}

package com.valenguard.server.game.world.task;

import com.google.common.base.Preconditions;
import com.valenguard.server.Server;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.item.inventory.BankActions;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.network.game.packet.out.BankManagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityMovePacketOut;

import static com.valenguard.server.util.Log.println;

public class MovementUpdateTask implements AbstractTask {

    private final static boolean PRINT_DEBUG = false;

    @Override
    public void tick(long ticksPassed) {
        Server.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);

        // Try and start an entity move
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(aiEntity -> generateNewAIMovements((AiEntity) aiEntity),
                entity -> !entity.isEntityMoving());

        // Continue entity movement
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(aiEntity -> updateEntitiesPosition((AiEntity) aiEntity),
                MovingEntity::isEntityMoving);
    }

    private void generateNewAIMovements(AiEntity aiEntity) {
        if (aiEntity.getEntityType() != EntityType.NPC && aiEntity.getEntityType() != EntityType.MONSTER)
            return;

        // We have a target to follow, so no random movements
        if (aiEntity.getTargetEntity() != null) return;

        MoveDirection moveDirection = (aiEntity).getRandomRegionMoveGenerator().generateMoveDirection(false);

        // Start performing a movement if the entity is not moving
        if (moveDirection != MoveDirection.NONE) {
            println(getClass(), "NPC has started moving.", false, PRINT_DEBUG);
            performAiEntityMove(aiEntity, moveDirection);
        }
    }

    private void updateEntitiesPosition(MovingEntity movingEntity) {

        moveEntity(movingEntity);

        if (movingEntity.getWalkTime() <= movingEntity.getMoveSpeed()) return;

        if (movingEntity instanceof Player) {

            Player player = (Player) movingEntity;
            finishMove(movingEntity);

            if (!player.getLatestMoveRequests().isEmpty()) {

                Location newAttemptLocation = player.getLatestMoveRequests().remove();

                if (player.getWarp() != null) return;

                performPlayerMove(player, newAttemptLocation);
            }
        } else {

            AiEntity aiEntity = (AiEntity) movingEntity;
            finishMove(aiEntity);

            println(getClass(), "Generating a new move.", false, PRINT_DEBUG);

            aiEntity.getRandomRegionMoveGenerator().setAlreadyDeterminedMove(false);

            if (aiEntity.getTargetEntity() != null) return;

            MoveDirection predictedMoveDirection = aiEntity.getRandomRegionMoveGenerator().generateMoveDirection(true);

            if (predictedMoveDirection != MoveDirection.NONE) {
                performAiEntityMove(aiEntity, predictedMoveDirection);
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

    public void initEntityTargeting(MovingEntity movingEntity) {
        GameMap gameMap = movingEntity.getGameMap();

        if (movingEntity instanceof Player) {
            /*
             * PLAYER FINISHED MOVE
             */

            // AiEntity find Player targets
            for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                findEntityTarget(aiEntity, movingEntity);
            }
        } else if (movingEntity instanceof AiEntity) {
            /*
             * AiENTITY FINISHED MOVE
             */
            AiEntity aiEntityFindTarget = (AiEntity) movingEntity;

            // AiEntity find AiEntity targets
            for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                if (movingEntity.equals(aiEntity)) continue;
                findEntityTarget(aiEntityFindTarget, aiEntity);
            }

            // AiEntity find Player targets
            for (Player player : gameMap.getPlayerController().getPlayerList()) {
                findEntityTarget(aiEntityFindTarget, player);
            }
        }
    }

    private void findEntityTarget(AiEntity attackerEntity, MovingEntity targetEntity) {
        Location attackerLocation = attackerEntity.getCurrentMapLocation();
        Location targetLocation = targetEntity.getCurrentMapLocation();

        // The attacker has no assigned target.
        if (attackerEntity.getTargetEntity() == null) {

            // Is attacker within distance to target
            if (attackerLocation.isWithinDistance(targetLocation, GameConstants.START_ATTACK_RADIUS)) {

                // If the attacker is hostile, then we assign the target.
                if (attackerEntity.getEntityType() == EntityType.MONSTER) {
                    if (((Monster) attackerEntity).getAlignment() == EntityAlignment.HOSTILE) {
                        if (attackerEntity.getEntityType() == targetEntity.getEntityType()) return;
                        attackerEntity.setTargetEntity(targetEntity);
                        findTrackingPath(attackerEntity, targetEntity);
                    }
                } else if (attackerEntity.getEntityType() == EntityType.NPC) {
                    byte attackerFaction = ((NPC) attackerEntity).getFaction();

                    /*
                     * NPC vs PLAYER
                     */
                    if (targetEntity.getEntityType() == EntityType.PLAYER) {

                        Player player = (Player) targetEntity;
                        EntityAlignment attackerAlignment = player.getReputation().getAlignment(attackerFaction);

                        if (attackerAlignment == EntityAlignment.HOSTILE) {
                            attackerEntity.setTargetEntity(targetEntity);
                            findTrackingPath(attackerEntity, targetEntity);
                        }
                    } else if (targetEntity.getEntityType() == EntityType.NPC) {
                        /*
                         * NPC vs NPC
                         */

                        byte[] factionEnemies = Server.getInstance().getFactionManager().getFactionEnemies(attackerFaction);
                        byte targetEntityFaction = ((NPC) targetEntity).getFaction();

                        for (byte enemy : factionEnemies) {
                            if (enemy == targetEntityFaction) {
                                attackerEntity.setTargetEntity(targetEntity);
                                findTrackingPath(attackerEntity, targetEntity);
                                break;
                            }
                        }
                    } else if (targetEntity.getEntityType() == EntityType.MONSTER) {
                        /*
                         * NPC vs MONSTER
                         */

                        if (((Monster) targetEntity).getAlignment() == EntityAlignment.HOSTILE) {
                            attackerEntity.setTargetEntity(targetEntity);
                            findTrackingPath(attackerEntity, targetEntity);
                        }
                    }
                }
            }
        } else if (attackerEntity.getTargetEntity().equals(targetEntity)) {

            // Target already assigned
            if (attackerLocation.isWithinDistance(targetLocation, GameConstants.QUIT_ATTACK_RADIUS)) {
                findTrackingPath(attackerEntity, targetEntity);
            } else {
                // The target is too far away, so remove their target.
                attackerEntity.setTargetEntity(null);
            }
        }
    }

    private void findTrackingPath(AiEntity aiEntity, MovingEntity targetEntity) {
        if (aiEntity.isEntityMoving()) return;

        GameMap gameMap = aiEntity.getGameMap();
        Location currentLocation = aiEntity.getCurrentMapLocation();
        Location targetLocation = targetEntity.getCurrentMapLocation();

        aiEntity.setTargetEntity(targetEntity);

        Location northLocation = currentLocation.add(gameMap.getLocation(MoveDirection.NORTH));
        Location southLocation = currentLocation.add(gameMap.getLocation(MoveDirection.SOUTH));
        Location eastLocation = currentLocation.add(gameMap.getLocation(MoveDirection.EAST));
        Location westLocation = currentLocation.add(gameMap.getLocation(MoveDirection.WEST));

        if (targetLocation.getX() > currentLocation.getX()) {
            if (!(currentLocation.getX() + 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(eastLocation)) performAiEntityMove(aiEntity, MoveDirection.EAST);
            }
        } else if (targetLocation.getX() < currentLocation.getX()) {
            if (!(currentLocation.getX() - 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(westLocation)) performAiEntityMove(aiEntity, MoveDirection.WEST);
            }
        } else if (targetLocation.getY() > currentLocation.getY()) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() + 1 == targetLocation.getY())) {
                if (gameMap.isMovable(northLocation)) performAiEntityMove(aiEntity, MoveDirection.NORTH);
            }
        } else if (targetLocation.getY() < currentLocation.getY()) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() - 1 == targetLocation.getY())) {
                if (gameMap.isMovable(southLocation)) performAiEntityMove(aiEntity, MoveDirection.SOUTH);
            }
        } else if (targetLocation.getX() == currentLocation.getX() && targetLocation.getY() == currentLocation.getY()) {
            // on top of each other
            if (gameMap.isMovable(northLocation)) {
                performAiEntityMove(aiEntity, MoveDirection.NORTH);
            } else if (gameMap.isMovable(southLocation)) {
                performAiEntityMove(aiEntity, MoveDirection.SOUTH);
            } else if (gameMap.isMovable(westLocation)) {
                performAiEntityMove(aiEntity, MoveDirection.WEST);
            } else if (gameMap.isMovable(eastLocation)) {
                performAiEntityMove(aiEntity, MoveDirection.EAST);
            } else {
                println(getClass(), "Setting target null??");
                aiEntity.setTargetEntity(null);
            }
        }
    }

    public boolean preMovementChecks(Player player, Location attemptLocation) {

        boolean playerIsMoving = player.isEntityMoving();
        boolean moveQueueEmpty = player.getLatestMoveRequests().isEmpty();

        // Makes sure they are not trying to move to where they already are located.
        if (!playerIsMoving) {
            if (attemptLocation.equals(player.getCurrentMapLocation())) {
                println(getClass(), "A packetReceiver tried to request a movement to the tile they are already on.", true);
                return false;
            }
        } else {

            if (moveQueueEmpty) {
                // We compare the incoming request up against where they will be in the future.
                if (attemptLocation.equals(player.getFutureMapLocation())) {
                    println(getClass(), "The packetReceiver tried to request movement to where their future move already is.", true);
                    return false;
                }
            } else {
                // We compare the incoming request up against the last element in the queue.
                if (attemptLocation.equals(player.getLatestMoveRequests().getLast())) {
                    println(getClass(), "The packetReceiver tried to request a move to where they will eventually end up at the end of their movements.", true);
                    return false;
                }
            }
        }

        // Trying to make sure they move to a tile beside themselves.
        // Cases:
        // 1. The packetReceiver is not moving -> the tile beside them is the tile beside their current location
        // 2. The packetReceiver is moving and the movement queue is empty -> the tile beside them is the tile next to where their future location is
        // 3. The packetReceiver is moving and the movement is is not empty -> the tile beside them is where they will be at the end of their queue

        if (!playerIsMoving) {
            if (!player.getCurrentMapLocation().isWithinDistance(attemptLocation, (short) 1)) {
                new EntityMovePacketOut(player, player, player.getFutureMapLocation()).sendPacket();
                return false;
            }
        } else {
            if (moveQueueEmpty) {
                if (!player.getFutureMapLocation().isWithinDistance(attemptLocation, (short) 1)) {
                    new EntityMovePacketOut(player, player, player.getFutureMapLocation()).sendPacket();
                    return false;
                }
            } else {
                if (!player.getLatestMoveRequests().getLast().isWithinDistance(attemptLocation, (short) 1)) {
                    new EntityMovePacketOut(player, player, player.getFutureMapLocation()).sendPacket();
                    return false;
                }
            }
        }

        if (player.getWarp() != null) return false; // Stop packetReceiver moving during warp start

        // Prevents the packetReceiver from moving places they are not allowed to go.
        if (!player.getGameMap().isMovable(attemptLocation)) return false;

        if (player.isEntityMoving()) {
            player.addFutureMoveToQueue(attemptLocation);
            return false;
        }

        return true;
    }


    /**
     * Adds a entity to the list of entities that need to be processed.
     *
     * @param player The entity ot add.
     */
    public void performPlayerMove(Player player, Location attemptLocation) {

        // Canceling trade for the packetReceiver.
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Players can not move when trading.");

        if (player.getGameMap().locationHasWarp(attemptLocation)) {
            player.setWarp(player.getGameMap().getWarpFromLocation(attemptLocation));
        }

        StationaryEntity stationaryEntity = Server.getInstance().getGameLoop().getProcessMining().getMiningNode(player);
        if (stationaryEntity != null) {
            if (!stationaryEntity.getCurrentMapLocation().isWithinDistance(attemptLocation, (short) 1)) {
                Server.getInstance().getGameLoop().getProcessMining().removePlayer(player);
            }
        }

        // Cannot move and have the bank open
        if (player.isBankOpen()) {
            new BankManagePacketOut(player, BankActions.SERVER_CLOSE).sendPacket();
            player.setBankOpen(false);
        }


        player.setFutureMapLocation(new Location(attemptLocation));
        player.setWalkTime(0f);
        player.setFacingDirection(player.getCurrentMapLocation().getMoveDirectionFromLocation(player.getFutureMapLocation()));

        Server.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
                new EntityMovePacketOut(clientHandler.getPlayer(), player, attemptLocation).sendPacket());
    }

    private void performAiEntityMove(AiEntity aiEntity, MoveDirection moveDirection) {
        Preconditions.checkArgument(moveDirection != MoveDirection.NONE, "The requested move direction was NONE!");


        Location futureLocation = new Location(aiEntity.getCurrentMapLocation()).add(aiEntity.getGameMap().getLocation(moveDirection));
        aiEntity.setFutureMapLocation(futureLocation);
        aiEntity.setWalkTime(0f);
        aiEntity.setFacingDirection(moveDirection);

        println(getClass(), "CurrentLocation: " + aiEntity.getCurrentMapLocation(), false, PRINT_DEBUG);
        println(getClass(), "FutureLocation: " + aiEntity.getFutureMapLocation(), false, PRINT_DEBUG);

        aiEntity.getGameMap().getPlayerController().getPlayerList().forEach(player ->
                new EntityMovePacketOut(player, aiEntity, aiEntity.getFutureMapLocation()).sendPacket());
    }
}

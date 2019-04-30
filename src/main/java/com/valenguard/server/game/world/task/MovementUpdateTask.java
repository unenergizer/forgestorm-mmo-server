package com.valenguard.server.game.world.task;

import com.valenguard.server.Server;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.MessageText;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.item.inventory.BankActions;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.network.game.packet.out.BankManagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityMovePacketOut;
import com.valenguard.server.util.MoveNode;
import com.valenguard.server.util.PathFinding;
import com.valenguard.server.util.RandomUtil;
import lombok.AllArgsConstructor;

import java.util.*;

import static com.valenguard.server.util.Log.println;

public class MovementUpdateTask implements AbstractTask {

    private final static boolean PRINT_DEBUG = false;

    @AllArgsConstructor
    private class MovementTargetInfo {
        private AiEntity tracker;
        private Integer locationData;
    }

    private final PathFinding pathFinding = new PathFinding();

    private Map<MovingEntity, List<MovementTargetInfo>> targetsLocations = new HashMap<>();

    @Override
    public void tick(long ticksPassed) {

        mapTargets();

        Server.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);

        // Try and start an entity move
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(this::generateNewAIMovements,
                entity -> !entity.isEntityMoving() && entity.getMoveNodes().isEmpty());

        // Try and continue move from move nodes
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(this::continueFromMoveNodes,
                aiEntity -> !aiEntity.isEntityMoving() && !aiEntity.getMoveNodes().isEmpty());

        // Continue entity movement
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);
    }

    private void continueFromMoveNodes(AiEntity aiEntity) {
        MoveNode moveNode = aiEntity.getMoveNodes().remove();
        Location currentLocation = aiEntity.getCurrentMapLocation();

        performAiEntityMove(aiEntity, currentLocation.getMoveDirectionFromLocation(
                new Location(aiEntity.getMapName(), moveNode.getWorldX(), moveNode.getWorldY())));
    }

    private void mapTargets() {
        targetsLocations.clear();
        Server.getInstance().getGameManager().forAllAiEntitiesFiltered(aiEntity -> {
                    Location location = aiEntity.getFutureMapLocation();
                    MovementTargetInfo movementTargetInfo = new MovementTargetInfo(aiEntity, conjoinShorts(location));
                    if (targetsLocations.containsKey(aiEntity.getTargetEntity())) {
                        targetsLocations.get(aiEntity.getTargetEntity()).add(movementTargetInfo);
                    } else {
                        List<MovementTargetInfo> locations = new ArrayList<>();
                        locations.add(movementTargetInfo);
                        targetsLocations.put(aiEntity.getTargetEntity(), locations);
                    }
                },
                aiEntity -> aiEntity.getTargetEntity() != null);
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
        if (!aiEntity.getMoveNodes().isEmpty()) return;

        GameMap gameMap = aiEntity.getGameMap();
        Location currentLocation = aiEntity.getCurrentMapLocation();
        Location targetLocation = targetEntity.getCurrentMapLocation();

        List<MovementTargetInfo> otherTargetLocations = targetsLocations.get(targetEntity);
        if (otherTargetLocations == null) otherTargetLocations = new ArrayList<>();

        aiEntity.setTargetEntity(targetEntity);

        Location northLocation = currentLocation.add(gameMap.getLocation(MoveDirection.NORTH));
        Location southLocation = currentLocation.add(gameMap.getLocation(MoveDirection.SOUTH));
        Location eastLocation = currentLocation.add(gameMap.getLocation(MoveDirection.EAST));
        Location westLocation = currentLocation.add(gameMap.getLocation(MoveDirection.WEST));

        // To the right of the entity && right location is not
        if (targetLocation.getX() > currentLocation.getX() && gameMap.isMovable(eastLocation) &&
                !containsMovement(otherTargetLocations, eastLocation, aiEntity)) {
            if (!(currentLocation.getX() + 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(eastLocation)) performAiEntityMove(aiEntity, MoveDirection.EAST);
            } else if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.EAST);
            }
        } else if (targetLocation.getX() < currentLocation.getX() && gameMap.isMovable(westLocation) &&
                !containsMovement(otherTargetLocations, westLocation, aiEntity)) {
            if (!(currentLocation.getX() - 1 == targetLocation.getX() && currentLocation.getY() == targetLocation.getY())) {
                if (gameMap.isMovable(westLocation)) performAiEntityMove(aiEntity, MoveDirection.WEST);
            } else if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.WEST);
            }
        } else if (targetLocation.getY() > currentLocation.getY() && gameMap.isMovable(northLocation) &&
                !containsMovement(otherTargetLocations, northLocation, aiEntity)) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() + 1 == targetLocation.getY())) {
                if (gameMap.isMovable(northLocation)) performAiEntityMove(aiEntity, MoveDirection.NORTH);
            } else if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.NORTH);
            }
        } else if (targetLocation.getY() < currentLocation.getY() && gameMap.isMovable(southLocation) &&
                !containsMovement(otherTargetLocations, southLocation, aiEntity)) {
            if (!(currentLocation.getX() == targetLocation.getX() && currentLocation.getY() - 1 == targetLocation.getY())) {
                if (gameMap.isMovable(southLocation)) performAiEntityMove(aiEntity, MoveDirection.SOUTH);
            } else if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.SOUTH);
            }

            // The entities are on top of each other
        } else if (targetLocation.getX() == currentLocation.getX() && targetLocation.getY() == currentLocation.getY()) {
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
        } else {
            if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.NONE);
            } else {

                Location north = targetLocation.add(gameMap.getLocation(MoveDirection.NORTH));
                Location south = targetLocation.add(gameMap.getLocation(MoveDirection.SOUTH));
                Location east = targetLocation.add(gameMap.getLocation(MoveDirection.EAST));
                Location west = targetLocation.add(gameMap.getLocation(MoveDirection.WEST));
                Location northEast = north.add(gameMap.getLocation(MoveDirection.EAST));
                Location northWest = north.add(gameMap.getLocation(MoveDirection.WEST));
                Location southEast = south.add(gameMap.getLocation(MoveDirection.EAST));
                Location southWest = south.add(gameMap.getLocation(MoveDirection.WEST));

                if (attemptAStar(otherTargetLocations, north, aiEntity)) return;
                if (attemptAStar(otherTargetLocations, south, aiEntity)) return;
                if (attemptAStar(otherTargetLocations, east, aiEntity)) return;
                if (attemptAStar(otherTargetLocations, west, aiEntity)) return;

                // The entity is already in one of the four corners
                if (currentLocation.equals(northEast) || currentLocation.equals(northWest) ||
                        currentLocation.equals(southEast) || currentLocation.equals(southWest)) {
                    return;
                }

                if (attemptAStar(otherTargetLocations, northEast, aiEntity)) return;
                if (attemptAStar(otherTargetLocations, northWest, aiEntity)) return;
                if (attemptAStar(otherTargetLocations, southEast, aiEntity)) return;
                attemptAStar(otherTargetLocations, southWest, aiEntity);

            }
        }
    }

    private boolean targetLocationMovable(List<MovementTargetInfo> otherTargetLocations, Location attemptLocation, AiEntity tracker) {
        if (!attemptLocation.getGameMap().isMovable(attemptLocation)) return false;
        if (!containsMovement(otherTargetLocations, attemptLocation, tracker)) return false;
        return true;
    }

    private boolean attemptAStar(List<MovementTargetInfo> otherTargetLocations, Location location, AiEntity tracker) {
        if (!location.getGameMap().isMovable(location)) return false;
        if (containsMovement(otherTargetLocations, location, tracker)) return false;
        Queue<MoveNode> nodes = pathFinding.findPath(tracker.getGameMap(),
                tracker.getCurrentMapLocation().getX(),
                tracker.getCurrentMapLocation().getY(),
                location.getX(),
                location.getY());
        if (nodes == null) return false;
        tracker.setMoveNodes(nodes);
        return true;
    }

    private void generateNewMoveAndExclude(List<MovementTargetInfo> otherTargetLocations, AiEntity aiEntity, MoveDirection excludeDirection) {
        otherTargetLocations.removeIf(info -> info.tracker.equals(aiEntity));
        List<MoveDirection> directions = new LinkedList<>(Arrays.asList(MoveDirection.EAST, MoveDirection.NORTH, MoveDirection.SOUTH, MoveDirection.WEST));
        directions.removeIf(direction -> direction == excludeDirection);
        MoveDirection attemptDirection = directions.get(RandomUtil.getNewRandom(0, 2));
        GameMap gameMap = aiEntity.getGameMap();
        if (gameMap.isMovable(aiEntity.getCurrentMapLocation().add(gameMap.getLocation(attemptDirection)))) {
            performAiEntityMove(aiEntity, attemptDirection);
        }
    }

    private boolean containsMovement(List<MovementTargetInfo> otherTargetLocations, Location testLocation, AiEntity tracker) {
        return otherTargetLocations.stream().anyMatch(info -> !info.tracker.equals(tracker) &&
                info.locationData.equals(conjoinShorts(testLocation)));
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
        Server.getInstance().getTradeManager().ifTradeExistCancel(player, MessageText.SERVER + "Trade canceled. Players can not move when trading.");

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
//        Preconditions.checkArgument(moveDirection != MoveDirection.NONE, "The requested move direction was NONE!");
        if (moveDirection == MoveDirection.NONE) return;

        Location futureLocation = new Location(aiEntity.getCurrentMapLocation()).add(aiEntity.getGameMap().getLocation(moveDirection));
        aiEntity.setFutureMapLocation(futureLocation);
        aiEntity.setWalkTime(0f);
        aiEntity.setFacingDirection(moveDirection);

        println(getClass(), "CurrentLocation: " + aiEntity.getCurrentMapLocation(), false, PRINT_DEBUG);
        println(getClass(), "FutureLocation: " + aiEntity.getFutureMapLocation(), false, PRINT_DEBUG);

        aiEntity.getGameMap().getPlayerController().getPlayerList().forEach(player ->
                new EntityMovePacketOut(player, aiEntity, aiEntity.getFutureMapLocation()).sendPacket());
    }

    private int conjoinShorts(Location location) {
        return (location.getX() << 16) | (location.getY() & 0xFFFF);
    }
}

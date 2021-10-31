package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.rpg.EntityAlignment;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.game.world.item.inventory.BankActions;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.WorldChunk;
import com.forgestorm.server.network.game.packet.out.BankManagePacketOutOut;
import com.forgestorm.server.network.game.packet.out.ClientMoveResyncPacketOutOut;
import com.forgestorm.server.network.game.packet.out.EntityMovePacketOutOut;
import com.forgestorm.server.util.MoveNode;
import com.forgestorm.server.util.PathFinding;
import com.forgestorm.shared.util.RandomNumberUtil;
import lombok.AllArgsConstructor;

import java.util.*;

import static com.forgestorm.server.util.Log.println;

public class MovementUpdateTask implements AbstractTask {

    private final static boolean PRINT_DEBUG = false;

    @AllArgsConstructor
    private static class MovementTargetInfo {
        private final AiEntity tracker;
        private Location location;
    }

    @AllArgsConstructor
    private static class RandomDirectionResult {
        private final MoveDirection moveDirection;
        private final Location attemptLocation;
    }

    private final PathFinding pathFinding = new PathFinding();

    private final Map<MovingEntity, List<MovementTargetInfo>> targetsLocations = new HashMap<>();

    @Override
    public void tick(long ticksPassed) {

        mapTargets();

        ServerMain.getInstance().getGameManager().forAllPlayersFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);

        // Try and start an entity move
        ServerMain.getInstance().getGameManager().forAllAiEntitiesFiltered(this::generateNewAIMovements,
                entity -> !entity.isEntityMoving() && entity.getMoveNodes().isEmpty());

        // Try and continue move from move nodes
        ServerMain.getInstance().getGameManager().forAllAiEntitiesFiltered(this::continueFromMoveNodes,
                aiEntity -> !aiEntity.isEntityMoving() && !aiEntity.getMoveNodes().isEmpty());

        // Continue entity movement
        ServerMain.getInstance().getGameManager().forAllAiEntitiesFiltered(this::updateEntitiesPosition, MovingEntity::isEntityMoving);
    }


    private void continueFromMoveNodes(AiEntity aiEntity) {
        MoveNode moveNode = aiEntity.getMoveNodes().remove();
        Location currentLocation = aiEntity.getCurrentWorldLocation();

        performAiEntityMove(aiEntity, currentLocation.getMoveDirectionFromLocation(
                new Location(aiEntity.getWorldName(), moveNode.getWorldX(), moveNode.getWorldY(), currentLocation.getZ())));
    }

    private void mapTargets() {
        targetsLocations.clear();
        ServerMain.getInstance().getGameManager().forAllAiEntitiesFiltered(aiEntity -> {
                    Location location = aiEntity.getFutureWorldLocation();
                    MovementTargetInfo movementTargetInfo = new MovementTargetInfo(aiEntity, location);
                    if (targetsLocations.containsKey(aiEntity.getTargetEntity())) {
                        targetsLocations.get(aiEntity.getTargetEntity()).add(movementTargetInfo);
                    } else {
                        List<MovementTargetInfo> locations = new ArrayList<>();
                        locations.add(movementTargetInfo);
                        targetsLocations.put(aiEntity.getTargetEntity(), locations);
                    }
                },
                aiEntity -> aiEntity.getTargetEntity() != null);

        targetsLocations.forEach((target, trackers) ->
                trackers.removeIf(tracker -> tracker.location.getDistanceAway(target.getFutureWorldLocation()) >= 2));

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

        if (movingEntity.getWalkTime() < 1.0F) return;

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
            if (!aiEntity.getMoveNodes().isEmpty()) return;

            MoveDirection predictedMoveDirection = aiEntity.getRandomRegionMoveGenerator().generateMoveDirection(true);

            if (predictedMoveDirection != MoveDirection.NONE) {
                performAiEntityMove(aiEntity, predictedMoveDirection);
            }

        }
    }

    private void moveEntity(MovingEntity movingEntity) {
        // TODO: Might want to get the actual delta (error incurred ter tick)
//        float delta = 1.0f / 20.0f;

        int currentX = movingEntity.getCurrentWorldLocation().getX();
        int currentY = movingEntity.getCurrentWorldLocation().getY();

        int futureX = movingEntity.getFutureWorldLocation().getX();
        int futureY = movingEntity.getFutureWorldLocation().getY();

        float frameMove = movingEntity.getMoveSpeed() / GameConstants.TICKS_PER_SECOND;

        movingEntity.setWalkTime(movingEntity.getWalkTime() + frameMove);

        movingEntity.setRealX(linearInterpolate(currentX, futureX, movingEntity.getWalkTime()) * GameConstants.TILE_SIZE);
        movingEntity.setRealY(linearInterpolate(currentY, futureY, movingEntity.getWalkTime()) * GameConstants.TILE_SIZE);
    }

    private float linearInterpolate(float start, float end, float a) {
        return start + (end - start) * a;
    }

    private void finishMove(MovingEntity movingEntity) {
        println(getClass(), "EntityId: " + movingEntity.getServerEntityId() + " has finished it's move", false, PRINT_DEBUG);

        movingEntity.getCurrentWorldLocation().set(movingEntity.getFutureWorldLocation());
        movingEntity.setRealX(movingEntity.getFutureWorldLocation().getX() * GameConstants.TILE_SIZE);
        movingEntity.setRealY(movingEntity.getFutureWorldLocation().getY() * GameConstants.TILE_SIZE);

        initEntityTargeting(movingEntity);
    }

    public void initEntityTargeting(MovingEntity movingEntity) {
        GameWorld gameWorld = movingEntity.getGameWorld();

        if (movingEntity instanceof Player) {
            /*
             * PLAYER FINISHED MOVE
             */

            // AiEntity find Player targets
            for (AiEntity aiEntity : gameWorld.getAiEntityController().getEntities()) {
                findEntityTarget(aiEntity, movingEntity);
            }
        } else if (movingEntity instanceof AiEntity) {
            /*
             * AiENTITY FINISHED MOVE
             */
            AiEntity aiEntityFindTarget = (AiEntity) movingEntity;

            // AiEntity find AiEntity targets
            for (AiEntity aiEntity : gameWorld.getAiEntityController().getEntities()) {
                if (movingEntity.equals(aiEntity)) continue;
                findEntityTarget(aiEntityFindTarget, aiEntity);
            }

            // AiEntity find Player targets
            for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                findEntityTarget(aiEntityFindTarget, player);
            }
        }
    }

    private void findEntityTarget(AiEntity attackerEntity, MovingEntity targetEntity) {
        Location attackerLocation = attackerEntity.getCurrentWorldLocation();
        Location targetLocation = targetEntity.getCurrentWorldLocation();

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

                        byte[] factionEnemies = ServerMain.getInstance().getFactionManager().getFactionEnemies(attackerFaction);
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

        GameWorld gameWorld = aiEntity.getGameWorld();
        Location currentLocation = aiEntity.getCurrentWorldLocation();
        Location targetLocation = targetEntity.getCurrentWorldLocation();

        List<MovementTargetInfo> otherTargetLocations = targetsLocations.get(targetEntity);
        if (otherTargetLocations == null) otherTargetLocations = new ArrayList<>();

        aiEntity.setTargetEntity(targetEntity);

        List<MoveDirection> directionOptions = new LinkedList<>(Arrays.asList(MoveDirection.EAST, MoveDirection.NORTH, MoveDirection.SOUTH, MoveDirection.WEST));

        int distanceAway = targetLocation.getDistanceAway(currentLocation);
        boolean currentLocationIsTaken = containsMovement(otherTargetLocations, currentLocation, aiEntity);
        if (distanceAway <= 2 && distanceAway != 0) {
            // They will go ahead and perform A* if they are this close
            if ((distanceAway == 1 && currentLocationIsTaken) ||
                    distanceAway == 2) {
                startAttemptAStar(otherTargetLocations, currentLocation, targetLocation, gameWorld, aiEntity, false);
            } else if (distanceAway == 1) {

                int xDiff = Math.abs(targetLocation.getX() - currentLocation.getX());
                int yDiff = Math.abs(targetLocation.getY() - currentLocation.getY());

                // At a diagonal to the entity

                if (xDiff + yDiff > 1) {
                    startAttemptAStar(otherTargetLocations, currentLocation, targetLocation, gameWorld, aiEntity, true);
                }

            }
            return;
        }

        MoveDirection attemptDirection1 = directionOptions.get(RandomNumberUtil.getNewRandom(0, 3));
        if (attemptAiTargetMove(currentLocation, targetLocation, gameWorld, attemptDirection1, otherTargetLocations, aiEntity)) {
            return;
        }

        directionOptions.removeIf(direction -> direction == attemptDirection1);
        MoveDirection attemptDirection2 = directionOptions.get(RandomNumberUtil.getNewRandom(0, 2));
        if (attemptAiTargetMove(currentLocation, targetLocation, gameWorld, attemptDirection2, otherTargetLocations, aiEntity)) {
            return;
        }

        directionOptions.removeIf(direction -> direction == attemptDirection2);
        MoveDirection attemptDirection3 = directionOptions.get(RandomNumberUtil.getNewRandom(0, 1));
        if (attemptAiTargetMove(currentLocation, targetLocation, gameWorld, attemptDirection3, otherTargetLocations, aiEntity)) {
            return;
        }

        directionOptions.removeIf(direction -> direction == attemptDirection3);
        MoveDirection attemptDirection4 = directionOptions.get(0);
        if (attemptAiTargetMove(currentLocation, targetLocation, gameWorld, attemptDirection4, otherTargetLocations, aiEntity)) {
            return;
        }

        if (targetLocation.getX() == currentLocation.getX() && targetLocation.getY() == currentLocation.getY()) {
            moveAiOffTarget(gameWorld, currentLocation, aiEntity);
            return;
        }

        if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
            generateNewMoveAndExclude(otherTargetLocations, aiEntity, MoveDirection.NONE);
        } else {
            startAttemptAStar(otherTargetLocations, currentLocation, targetLocation, gameWorld, aiEntity, false);
        }
    }

    private void startAttemptAStar(List<MovementTargetInfo> otherTargetLocations, Location currentLocation,
                                   Location targetLocation, GameWorld gameWorld, AiEntity aiEntity,
                                   boolean attemptSidesOnly) {

        List<Location> besideLocations = new ArrayList<>();

        besideLocations.add(targetLocation.add(gameWorld.getLocation(MoveDirection.NORTH)));
        besideLocations.add(targetLocation.add(gameWorld.getLocation(MoveDirection.SOUTH)));
        besideLocations.add(targetLocation.add(gameWorld.getLocation(MoveDirection.EAST)));
        besideLocations.add(targetLocation.add(gameWorld.getLocation(MoveDirection.WEST)));

        List<Location> diagonalLocations = new ArrayList<>();
        diagonalLocations.add(besideLocations.get(0).add(gameWorld.getLocation(MoveDirection.EAST)));
        diagonalLocations.add(besideLocations.get(0).add(gameWorld.getLocation(MoveDirection.WEST)));
        diagonalLocations.add(besideLocations.get(1).add(gameWorld.getLocation(MoveDirection.EAST)));
        diagonalLocations.add(besideLocations.get(1).add(gameWorld.getLocation(MoveDirection.WEST)));

        besideLocations.sort(Comparator.comparingInt(lhs -> lhs.getDistanceAway(currentLocation)));

        for (Location location : besideLocations) {
            if (attemptAStar(otherTargetLocations, location, aiEntity)) return;
        }

        diagonalLocations.sort(Comparator.comparingInt(lhs -> lhs.getDistanceAway(currentLocation)));

        if (!attemptSidesOnly) {
            for (Location location : diagonalLocations) {
                if (attemptAStar(otherTargetLocations, location, aiEntity)) return;
            }
        }
    }

    private void moveAiOffTarget(GameWorld gameWorld, Location currentLocation, AiEntity aiEntity) {
        List<MoveDirection> directionOptions = new LinkedList<>(Arrays.asList(MoveDirection.EAST, MoveDirection.NORTH, MoveDirection.SOUTH, MoveDirection.WEST));

        RandomDirectionResult result = genNewRandomAttemptLocation(directionOptions, MoveDirection.NONE, gameWorld, currentLocation);
        for (int i = 0; i < 4; i++) {
            if (gameWorld.isTraversable(result.attemptLocation)) {
                performAiEntityMove(aiEntity, result.moveDirection);
                return;
            }
            if (i + 1 != 4) {
                result = genNewRandomAttemptLocation(directionOptions, result.moveDirection, gameWorld, currentLocation);
            }
        }

        println(getClass(), "Setting target null??", false);
        aiEntity.setTargetEntity(null);
    }

    private RandomDirectionResult genNewRandomAttemptLocation(List<MoveDirection> directionOptions, MoveDirection previousMove,
                                                              GameWorld gameWorld, Location currentLocation) {
        if (previousMove != MoveDirection.NONE) directionOptions.removeIf(direction -> direction == previousMove);
        MoveDirection attemptDirection = directionOptions.get(RandomNumberUtil.getNewRandom(0, directionOptions.size() - 1));
        return new RandomDirectionResult(attemptDirection, currentLocation.add(gameWorld.getLocation(attemptDirection)));
    }

    private boolean attemptAiTargetMove(Location currentLocation, Location targetLocation, GameWorld gameWorld,
                                        MoveDirection attemptDirection, List<MovementTargetInfo> otherTargetLocations,
                                        AiEntity aiEntity) {

        Location attemptLocation = currentLocation.add(gameWorld.getLocation(attemptDirection));

        boolean directionComparison = false;
        if (attemptDirection == MoveDirection.NORTH) {
            directionComparison = targetLocation.getY() > currentLocation.getY();
        } else if (attemptDirection == MoveDirection.EAST) {
            directionComparison = targetLocation.getX() > currentLocation.getX();
        } else if (attemptDirection == MoveDirection.SOUTH) {
            directionComparison = targetLocation.getY() < currentLocation.getY();
        } else if (attemptDirection == MoveDirection.WEST) {
            directionComparison = targetLocation.getX() < currentLocation.getX();
        }

        if (directionComparison && gameWorld.isTraversable(attemptLocation)) {

            if (!(attemptLocation.equals(targetLocation))) {
                if (gameWorld.isTraversable(attemptLocation)) performAiEntityMove(aiEntity, attemptDirection);
            } else if (containsMovement(otherTargetLocations, currentLocation, aiEntity)) {
                generateNewMoveAndExclude(otherTargetLocations, aiEntity, attemptDirection);
            }
            return true;
        }
        return false;
    }

    private boolean attemptAStar(List<MovementTargetInfo> otherTargetLocations, Location location, AiEntity tracker) {
        if (!location.getGameWorld().isTraversable(location)) return false;
        if (containsMovement(otherTargetLocations, location, tracker)) return false;
        Queue<MoveNode> nodes = pathFinding.findPath(tracker.getGameWorld(),
                tracker.getCurrentWorldLocation().getX(),
                tracker.getCurrentWorldLocation().getY(),
                location.getX(),
                location.getY(),
                location.getZ());
        if (nodes == null) return false;
        tracker.setMoveNodes(nodes);
        MoveNode moveNode = nodes.peek();
        if (moveNode != null) {
            updateFutureLocation(tracker, new Location(moveNode.getMapName(), moveNode.getWorldX(), moveNode.getWorldY(), location.getZ()));
        }
        return true;
    }

    private void updateFutureLocation(AiEntity aiEntity, Location futureLocation) {
        if (aiEntity.getTargetEntity() != null) {
            List<MovementTargetInfo> targetInfoList = targetsLocations.get(aiEntity.getTargetEntity());
            if (targetInfoList != null) {
                targetInfoList.forEach(targetInfo -> {
                    if (targetInfo.tracker.equals(aiEntity)) {
                        targetInfo.location = futureLocation;
                    }
                });
            }
        }
    }

    private void generateNewMoveAndExclude(List<MovementTargetInfo> otherTargetLocations, AiEntity aiEntity, MoveDirection excludeDirection) {
        otherTargetLocations.removeIf(info -> info.tracker.equals(aiEntity));
        List<MoveDirection> directions = new LinkedList<>(Arrays.asList(MoveDirection.EAST, MoveDirection.NORTH, MoveDirection.SOUTH, MoveDirection.WEST));
        directions.removeIf(direction -> direction == excludeDirection);
        MoveDirection attemptDirection = directions.get(RandomNumberUtil.getNewRandom(0, 2));
        GameWorld gameWorld = aiEntity.getGameWorld();
        if (gameWorld.isTraversable(aiEntity.getCurrentWorldLocation().add(gameWorld.getLocation(attemptDirection)))) {
            performAiEntityMove(aiEntity, attemptDirection);
        }
    }

    private boolean containsMovement(List<MovementTargetInfo> otherTargetLocations, Location testLocation, AiEntity tracker) {
        return otherTargetLocations.stream().anyMatch(info -> !info.tracker.equals(tracker) &&
                info.location.equals(testLocation));
    }

    public boolean preMovementChecks(Player player, Location attemptLocation) {

        boolean playerIsMoving = player.isEntityMoving();
        boolean moveQueueEmpty = player.getLatestMoveRequests().isEmpty();

        // Makes sure they are not trying to move to where they already are located.
        if (!playerIsMoving) {
            if (attemptLocation.equals(player.getCurrentWorldLocation())) {
                println(getClass(), "A player tried to request a movement to the tile they are already on.", true);
                return false;
            }
        } else {

            if (moveQueueEmpty) {
                // We compare the incoming request up against where they will be in the future.
                if (attemptLocation.equals(player.getFutureWorldLocation())) {
                    println(getClass(), "The player tried to request movement to where their future move already is.", true);
                    return false;
                }
            } else {
                // We compare the incoming request up against the last element in the queue.
                if (attemptLocation.equals(player.getLatestMoveRequests().getLast())) {
                    println(getClass(), "The player tried to request a move to where they will eventually end up at the end of their movements.", true);
                    return false;
                }
            }
        }

        //    RESYNCING
        // Trying to make sure they move to a tile beside themselves.
        // Cases:
        // 1. The player is not moving -> the tile beside them is the tile beside their current location
        // 2. The player is moving and the movement queue is empty -> the tile beside them is the tile next to where their future location is
        // 3. The player is moving and the movement is is not empty -> the tile beside them is where they will be at the end of their queue

        if (!playerIsMoving) {
            if (!player.getCurrentWorldLocation().isWithinDistance(attemptLocation, (short) 1)) {
                new ClientMoveResyncPacketOutOut(player, player.getFutureWorldLocation()).sendPacket();
                return false;
            }
        } else {
            if (moveQueueEmpty) {
                if (!player.getFutureWorldLocation().isWithinDistance(attemptLocation, (short) 1)) {
                    new ClientMoveResyncPacketOutOut(player, player.getFutureWorldLocation()).sendPacket();
                    return false;
                }
            } else {
                if (!player.getLatestMoveRequests().getLast().isWithinDistance(attemptLocation, (short) 1)) {
                    new ClientMoveResyncPacketOutOut(player, player.getFutureWorldLocation()).sendPacket();
                    return false;
                }
            }
        }

        if (player.getWarp() != null) return false; // Stop player moving during warp start

        // Prevents the player from moving places they are not allowed to go.
        if (!player.getGameWorld().isTraversable(attemptLocation) && !player.isBypassCollision()) return false;

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
        //
        if (!preMovementChecks(player, attemptLocation)) {
            println(getClass(), "Player failed pre-movement checks.", false, PRINT_DEBUG);
            return;
        }

        // Canceling trade for the packetReceiver.
        ServerMain.getInstance().getTradeManager().ifTradeExistCancel(player, MessageText.SERVER + "Trade canceled. Players can not move when trading.");

        // Warping to location
        if (player.getGameWorld().locationHasWarp(attemptLocation)) {
            player.setWarp(player.getGameWorld().getWarpFromLocation(attemptLocation));
        }

        // Stationary Entity interactivity?
        StationaryEntity stationaryEntity = ServerMain.getInstance().getGameLoop().getProcessMining().getMiningNode(player);
        if (stationaryEntity != null) {
            if (!stationaryEntity.getCurrentWorldLocation().isWithinDistance(attemptLocation, (short) 1)) {
                ServerMain.getInstance().getGameLoop().getProcessMining().removePlayer(player);
            }
        }

        // Cannot move and have the bank open
        if (player.isBankOpen()) {
            new BankManagePacketOutOut(player, BankActions.SERVER_CLOSE).sendPacket();
            player.setBankOpen(false);
        }

        // Update future location
        player.setFutureWorldLocation(new Location(attemptLocation));
        player.setWalkTime(0f);
        player.setFacingDirection(player.getCurrentWorldLocation().getMoveDirectionFromLocation(player.getFutureWorldLocation()));

        // Compare chunks
        WorldChunk currentChunk = player.getCurrentWorldLocation().getLocationChunk();
        WorldChunk futureChunk = player.getFutureWorldLocation().getLocationChunk();

        // Generate new chunk
        if (futureChunk == null) {
            Location futureLocation = player.getFutureWorldLocation();
            short chunkX = (short) Math.floor(futureLocation.getX() / (float) GameConstants.CHUNK_SIZE);
            short chunkY = (short) Math.floor(futureLocation.getY() / (float) GameConstants.CHUNK_SIZE);
            futureChunk = player.getGameWorld().generateNewChunk(chunkX, chunkY);
        }

        // Send chunk
        if (!player.getGameWorld().isSameChunk(currentChunk, futureChunk)) {
            println(getClass(), "Moving into new chunk! Chunk XY: " + futureChunk.getChunkX() + " / " + futureChunk.getChunkY(), false, false);

            // The player is moving into a new chunk. Load chunks the player hasn't seen yet.
            // TODO: This loads chunks around the player...
            //  However, we should only send the player new chunks they don't have loaded yet...

            Location futureLocation = player.getFutureWorldLocation();
            short clientChunkX = (short) Math.floor(futureLocation.getX() / (float) GameConstants.CHUNK_SIZE);
            short clientChunkY = (short) Math.floor(futureLocation.getY() / (float) GameConstants.CHUNK_SIZE);

            for (int chunkY = clientChunkY - GameConstants.CHUNK_RADIUS; chunkY < clientChunkY + GameConstants.CHUNK_RADIUS + 1; chunkY++) {
                for (int chunkX = clientChunkX - GameConstants.CHUNK_RADIUS; chunkX < clientChunkX + GameConstants.CHUNK_RADIUS + 1; chunkX++) {
                    WorldChunk worldChunk = player.getGameWorld().getChunk((short) chunkX, (short) chunkY);
                    if (worldChunk != null) worldChunk.sendChunk(player);
                }
            }
        }

        ServerMain.getInstance().getGameManager().sendToAll(player, clientHandler ->
                new EntityMovePacketOutOut(clientHandler.getPlayer(), player, attemptLocation).sendPacket());

    }

    private void performAiEntityMove(AiEntity aiEntity, MoveDirection moveDirection) {
//        Preconditions.checkArgument(moveDirection != MoveDirection.NONE, "The requested move direction was NONE!");
        if (moveDirection == MoveDirection.NONE) return;
        if (aiEntity.isEntityMoving()) {
            println(getClass(), "The Entity is already moving!", true, PRINT_DEBUG);
        }

        Location futureLocation = new Location(aiEntity.getCurrentWorldLocation()).add(aiEntity.getGameWorld().getLocation(moveDirection));

        // Stopping entity from moving/chasing outside the region
        if (!aiEntity.isAiEntityInRegion(futureLocation)) return;

        aiEntity.setFutureWorldLocation(futureLocation);
        aiEntity.setWalkTime(0f);
        aiEntity.setFacingDirection(moveDirection);

        updateFutureLocation(aiEntity, futureLocation);

        println(getClass(), "CurrentLocation: " + aiEntity.getCurrentWorldLocation(), false, PRINT_DEBUG);
        println(getClass(), "FutureLocation: " + aiEntity.getFutureWorldLocation(), false, PRINT_DEBUG);

        aiEntity.getGameWorld().getPlayerController().getPlayerList().forEach(player ->
                new EntityMovePacketOutOut(player, aiEntity, aiEntity.getFutureWorldLocation()).sendPacket());
    }
}

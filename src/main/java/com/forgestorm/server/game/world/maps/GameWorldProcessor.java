package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.database.sql.GameWorldItemStackDropSQL;
import com.forgestorm.server.database.sql.GameWorldMonsterSQL;
import com.forgestorm.server.database.sql.GameWorldNpcSQL;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.io.todo.FileManager;
import lombok.Getter;

import java.io.File;
import java.util.*;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameWorldProcessor {

    private static final boolean PRINT_DEBUG = true;

    @Getter
    private final Map<String, GameWorld> gameWorlds = new HashMap<>();

    /**
     * These queues are needed because entities need to be queued to spawn before
     * the world object is created.
     */
    private final Queue<AiEntity> aiEntitySpawnQueue = new LinkedList<>();
    private final Queue<StationaryEntity> stationaryEntitySpawnQueue = new LinkedList<>();
    private final Queue<ItemStackDrop> ItemStackDropSpawnQueue = new LinkedList<>();

    public void queueAiEntitySpawn(AiEntity aiEntity) {
        aiEntitySpawnQueue.add(aiEntity);
    }

    public void queueStationaryEntitySpawn(StationaryEntity stationaryEntity) {
        stationaryEntitySpawnQueue.add(stationaryEntity);
    }

    public void queueItemStackDropSpawn(ItemStackDrop itemStackDrop) {
        ItemStackDropSpawnQueue.add(itemStackDrop);
    }

    public void spawnEntities() {
        AiEntity aiEntity;
        while ((aiEntity = aiEntitySpawnQueue.poll()) != null) {
            aiEntity.getGameWorld().getAiEntityController().queueEntitySpawn(aiEntity);
        }
        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntitySpawnQueue.poll()) != null) {
            stationaryEntity.getGameWorld().getStationaryEntityController().queueEntitySpawn(stationaryEntity);
        }
        ItemStackDrop itemStackDrop;
        while ((itemStackDrop = ItemStackDropSpawnQueue.poll()) != null) {
            itemStackDrop.getGameWorld().getItemStackDropEntityController().queueEntitySpawn(itemStackDrop);
        }
    }

    public void playerSwitchGameWorld(Player player) {
        String currentWorldName = player.getWorldName();
        Warp warp = player.getWarp();

        gameWorlds.get(currentWorldName).getPlayerController().removePlayer(player);
        gameWorlds.get(warp.getLocation().getWorldName()).getPlayerController().addPlayer(player, warp);
        player.setWarp(null);
    }

    public void loadAllWorlds() {
        FileManager fileManager = ServerMain.getInstance().getFileManager();
        File[] files = new File(fileManager.getWorldDirectory()).listFiles((d, name) -> name.endsWith(GameConstants.MAP_FILE_EXTENSION_TYPE));
        checkNotNull(files, "No game worlds were found.");

        for (File file : files) {
            String path = fileManager.loadGameWorldData(file);
            GameWorld gameWorld = fileManager.getGameWorldData(path).getGameWorld();
            loadWorld(gameWorld);
        }

        if (files.length == 0) createDefaultGameWorld(fileManager);

        println(getClass(), "Game Worlds Loaded: " + gameWorlds.size());
        if (gameWorlds.size() == 0) throw new RuntimeException("No worlds loaded. Shutting down...");
    }

    private void createDefaultGameWorld(FileManager fileManager) {
        println(getClass(), "No game worlds exist, creating one now.", true);
        String worldName = "game_start";
        File filePath = new File(fileManager.getWorldDirectory() + File.separator + worldName + GameConstants.MAP_FILE_EXTENSION_TYPE);
        WorldCreator worldCreator = new WorldCreator();
        worldCreator.createWorld(worldName, 1, 1);
        String path = fileManager.loadGameWorldData(filePath);
        GameWorld gameWorld = fileManager.getGameWorldData(path).getGameWorld();
        loadWorld(gameWorld);
    }

    public void loadWorld(GameWorld gameWorld) {
        gameWorld.loadChunks();
        gameWorlds.put(gameWorld.getWorldName(), gameWorld);
    }

    public void getEntitiesFromDatabase() {
        println(getClass(), "Loading entities from database has started...");
        for (GameWorld gameWorld : gameWorlds.values()) {
            loadEntities(gameWorld);
        }
        println(getClass(), "Loading entities from database finished!");
    }

    public void loadEntities(GameWorld gameWorld) {
        int npcSize = loadNPC(gameWorld);
        int monsterSize = loadMonster(gameWorld);
        int itemStackDropSize = loadItemStackDrop(gameWorld);
        int entityTotal = npcSize + monsterSize + itemStackDropSize;
        println(getClass(), "World: " + gameWorld.getWorldName() +
                ", NPCs Total: " + npcSize +
                ", Monsters Total: " + monsterSize +
                ", ItemStackDrop Total: " + itemStackDropSize +
                ", Entity Total: " + entityTotal, false, PRINT_DEBUG);
    }

    public int loadNPC(GameWorld gameWorld) {
        GameWorldNpcSQL gameWorldNpcSQL = new GameWorldNpcSQL();
        List<Integer> npcIdList = gameWorldNpcSQL.searchNPC(gameWorld.getWorldName());

        for (Integer i : npcIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameWorld, EntityType.NPC)) continue;

            NPC npc = new NPC();
            npc.setFacingDirection(MoveDirection.SOUTH);
            npc.setEntityType(EntityType.NPC);
            npc.setAppearance(new Appearance(npc));
            npc.setDatabaseId(i);

            gameWorldNpcSQL.loadSQL(npc);

            queueAiEntitySpawn(npc);
        }

        return npcIdList.size();
    }

    public int loadMonster(GameWorld gameWorld) {
        GameWorldMonsterSQL gameWorldMonsterSQL = new GameWorldMonsterSQL();
        List<Integer> monsterIdList = gameWorldMonsterSQL.searchMonster(gameWorld.getWorldName());

        for (Integer i : monsterIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameWorld, EntityType.MONSTER)) continue;

            Monster monster = new Monster();
            monster.setFacingDirection(MoveDirection.SOUTH);
            monster.setEntityType(EntityType.MONSTER);
            monster.setAppearance(new Appearance(monster));
            monster.setDatabaseId(i);

            gameWorldMonsterSQL.loadSQL(monster);

            queueAiEntitySpawn(monster);
        }

        return monsterIdList.size();
    }

    public int loadItemStackDrop(GameWorld gameWorld) {
        GameWorldItemStackDropSQL gameWorldItemStackDropSQL = new GameWorldItemStackDropSQL();
        List<Integer> monsterIdList = gameWorldItemStackDropSQL.searchItemStackDrop(gameWorld.getWorldName());

        for (Integer i : monsterIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameWorld, EntityType.MONSTER)) continue;

            ItemStackDrop itemStackDrop = new ItemStackDrop();
            itemStackDrop.setEntityType(EntityType.ITEM_STACK);
            itemStackDrop.setDatabaseId(i);

            gameWorldItemStackDropSQL.loadSQL(itemStackDrop);

            queueItemStackDropSpawn(itemStackDrop);
        }

        return monsterIdList.size();
    }

    private boolean alreadySpawnedCheck(int databaseId, GameWorld gameWorld, EntityType entityType) {
        boolean continueLoop = false;
        for (AiEntity aiEntity : gameWorld.getAiEntityController().getEntities()) {
            if (aiEntity.getDatabaseId() == databaseId && aiEntity.getEntityType() == entityType) {
                continueLoop = true;
                break;
            }
        }
        return continueLoop;
    }

    public GameWorld getGameWorld(String worldName) throws RuntimeException {
        checkNotNull(gameWorlds.get(worldName), "Tried to get the world " + worldName + ", but it doesn't exist or was not loaded.");
        return gameWorlds.get(worldName);
    }

    public boolean doesGameWorldExist(String worldName) {
        return gameWorlds.containsKey(worldName);
    }
}

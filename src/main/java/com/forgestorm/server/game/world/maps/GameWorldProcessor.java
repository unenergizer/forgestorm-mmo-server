package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.database.sql.GameWorldItemStackDropSQL;
import com.forgestorm.server.database.sql.GameWorldMonsterSQL;
import com.forgestorm.server.database.sql.GameWorldNpcSQL;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.io.FilePaths;
import com.forgestorm.server.io.JsonMapParser;
import com.forgestorm.server.io.ResourceList;
import lombok.Getter;

import java.io.File;
import java.util.*;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameWorldProcessor {

    private static final String EXTENSION_TYPE = ".json";
    private static final boolean PRINT_DEBUG = true;

    @Getter
    private final Map<String, GameWorld> gameMaps = new HashMap<>();

    /**
     * These queues are needed because entities need to be queued to spawn before
     * the map object is created.
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
            aiEntity.getGameMap().getAiEntityController().queueEntitySpawn(aiEntity);
        }
        StationaryEntity stationaryEntity;
        while ((stationaryEntity = stationaryEntitySpawnQueue.poll()) != null) {
            stationaryEntity.getGameMap().getStationaryEntityController().queueEntitySpawn(stationaryEntity);
        }
        ItemStackDrop itemStackDrop;
        while ((itemStackDrop = ItemStackDropSpawnQueue.poll()) != null) {
            itemStackDrop.getGameMap().getItemStackDropEntityController().queueEntitySpawn(itemStackDrop);
        }
    }

    public void playerSwitchGameMap(Player player) {
        String currentMapName = player.getMapName();
        Warp warp = player.getWarp();

        println(getClass(), warp.getLocation().getWorldName(), true);

        gameMaps.get(currentMapName).getPlayerController().removePlayer(player);
        gameMaps.get(warp.getLocation().getWorldName()).getPlayerController().addPlayer(player, warp);
        player.setWarp(null);
    }

    public void loadAllMaps() {
        // TEST CHUNK LOADER AND GAME WORLD LOADER
        ServerMain.getInstance().getFileManager().loadGameWorldData();
        ServerMain.getInstance().getFileManager().loadMapChunkData("game_start", (short) 0, (short) 0, true);

        println(getClass(),"GameWorlds: " + ServerMain.getInstance().getFileManager().getGameWorldData().getGameWorlds().size());
        println(getClass(),"Does Chunk Exist: " + ServerMain.getInstance().getFileManager().getMapChunkData("game_start", (short) 0, (short) 0).toString());

        int mapCount;

        if (ServerMain.ideRun) {
            mapCount = loadIdeVersion();
        } else {
            mapCount = loadJarVersion();
        }
        println(getClass(), "Game Maps Loaded: " + mapCount);

//        fixWarpHeights(); // TODO - FIX ME
    }

    private int loadIdeVersion() {
        File[] files = new File("src/main/resources/" + FilePaths.MAPS.getFilePath()).listFiles((d, name) -> name.endsWith(EXTENSION_TYPE));
        checkNotNull(files, "No game maps were loaded.");

        for (File file : files) {
            loadMap(JsonMapParser.load(file.getName()));
        }

        return files.length;
    }

    private int loadJarVersion() {
        Collection<String> files = ResourceList.getDirectoryResources(FilePaths.MAPS.getFilePath(), EXTENSION_TYPE);
        checkNotNull(files, "No game maps were loaded.");

        for (String fileName : files) {
            String[] temp = fileName.split("/"); // Removes the path
            loadMap(JsonMapParser.load(temp[temp.length - 1]));
        }

        return files.size();
    }

    public void loadMap(GameWorld gameWorld) {
        gameMaps.put(gameWorld.getWorldName(), gameWorld);
    }

    public void getEntitiesFromDatabase() {
        println(getClass(), "Loading entities from database has started...");
        for (GameWorld gameWorld : gameMaps.values()) {
            loadEntities(gameWorld);
        }
        println(getClass(), "Loading entities from database finished!");
    }

    public void loadEntities(GameWorld gameWorld) {
        int npcSize = loadNPC(gameWorld);
        int monsterSize = loadMonster(gameWorld);
        int itemStackDropSize = loadItemStackDrop(gameWorld);
        int entityTotal = npcSize + monsterSize + itemStackDropSize;
        println(getClass(), "Map: " + gameWorld.getWorldName() +
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

    public GameWorld getGameMap(String mapName) throws RuntimeException {
        checkNotNull(gameMaps.get(mapName), "Tried to get the map " + mapName + ", but it doesn't exist or was not loaded.");
        return gameMaps.get(mapName);
    }

    public boolean doesGameMapExist(String mapName) {
        return gameMaps.containsKey(mapName);
    }

    public boolean doesLocationExist(Location location) {
        if (!doesGameMapExist(location.getWorldName())) return false;
        return !gameMaps.get(location.getWorldName()).isOutOfBounds(location.getX(), location.getY());
    }
}

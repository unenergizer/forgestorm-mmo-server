package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.database.sql.GameWorldItemStackDropSQL;
import com.forgestorm.server.database.sql.GameWorldMonsterSQL;
import com.forgestorm.server.database.sql.GameWorldNpcSQL;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.io.FilePaths;
import com.forgestorm.server.io.TmxFileParser;
import lombok.Getter;

import java.io.File;
import java.util.*;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameMapProcessor {

    @Getter
    private final Map<String, GameMap> gameMaps = new HashMap<>();

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

        gameMaps.get(currentMapName).getPlayerController().removePlayer(player);
        gameMaps.get(warp.getLocation().getMapName()).getPlayerController().addPlayer(player, warp);
        player.setWarp(null);
    }

    public void loadAllMaps() {
        File[] files = new File(FilePaths.MAPS.getFilePath()).listFiles((d, name) -> name.endsWith(".tmx"));
        checkNotNull(files, "No game maps were loaded.");

        for (File file : files) {
            String mapName = file.getName().replace(".tmx", "");
            loadMap(TmxFileParser.parseGameMap(FilePaths.MAPS.getFilePath(), mapName));
        }

        println(getClass(), "Tmx Maps Loaded: " + files.length);
        fixWarpHeights();
    }

    public void loadMap(GameMap gameMap) {
        gameMaps.put(gameMap.getMapName(), gameMap);
    }

    public void getEntitiesFromDatabase() {
        println(getClass(), "Loading entities from database has started...");
        for (GameMap gameMap : gameMaps.values()) {
            loadEntities(gameMap);
        }
        println(getClass(), "Loading entities from database finished!");
    }

    public void loadEntities(GameMap gameMap) {
        int npcSize = loadNPC(gameMap);
        int monsterSize = loadMonster(gameMap);
        int itemStackDropSize = loadItemStackDrop(gameMap);
        int entityTotal = npcSize + monsterSize + itemStackDropSize;
        println(getClass(), "Map:" + gameMap.getMapName() +
                ", NPCs Total: " + npcSize +
                ", Monsters Total: " + monsterSize +
                ", ItemStackDrop Total: " + itemStackDropSize +
                ", Entity Total: " + entityTotal);
    }

    public int loadNPC(GameMap gameMap) {
        GameWorldNpcSQL gameWorldNpcSQL = new GameWorldNpcSQL();
        List<Integer> npcIdList = gameWorldNpcSQL.searchNPC(gameMap.getMapName());

        for (Integer i : npcIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameMap, EntityType.NPC)) continue;

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

    public int loadMonster(GameMap gameMap) {
        GameWorldMonsterSQL gameWorldMonsterSQL = new GameWorldMonsterSQL();
        List<Integer> monsterIdList = gameWorldMonsterSQL.searchMonster(gameMap.getMapName());

        for (Integer i : monsterIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameMap, EntityType.MONSTER)) continue;

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

    public int loadItemStackDrop(GameMap gameMap) {
        GameWorldItemStackDropSQL gameWorldItemStackDropSQL = new GameWorldItemStackDropSQL();
        List<Integer> monsterIdList = gameWorldItemStackDropSQL.searchItemStackDrop(gameMap.getMapName());

        for (Integer i : monsterIdList) {

            // Don't spawn the entity if it is already spawned.
            if (alreadySpawnedCheck(i, gameMap, EntityType.MONSTER)) continue;

            ItemStackDrop itemStackDrop = new ItemStackDrop();
            itemStackDrop.setEntityType(EntityType.ITEM_STACK);
            itemStackDrop.setDatabaseId(i);

            gameWorldItemStackDropSQL.loadSQL(itemStackDrop);

            queueItemStackDropSpawn(itemStackDrop);
        }

        return monsterIdList.size();
    }

    private boolean alreadySpawnedCheck(int databaseId, GameMap gameMap, EntityType entityType) {
        boolean continueLoop = false;
        for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
            if (aiEntity.getDatabaseId() == databaseId && aiEntity.getEntityType() == entityType) {
                continueLoop = true;
                break;
            }
        }
        return continueLoop;
    }

    private void fixWarpHeights() {
        for (GameMap gameMap : gameMaps.values()) {
            println(getClass(), "Fixing warps for: " + gameMap.getMapName());
            for (short i = 0; i < gameMap.getMapWidth(); i++) {
                for (short j = 0; j < gameMap.getMapHeight(); j++) {
                    if (gameMap.isOutOfBounds(i, j)) continue;
                    Warp warp = gameMap.getMap()[i][j].getWarp();
                    if (warp == null) continue;
                    warp.getLocation().setY((short) (gameMaps.get(warp.getLocation().getMapName()).getMapHeight() - warp.getLocation().getY() - 1));
                }
            }
        }
    }

    public GameMap getGameMap(String mapName) throws RuntimeException {
        checkNotNull(gameMaps.get(mapName), "Tried to get the map " + mapName + ", but it doesn't exist or was not loaded.");
        return gameMaps.get(mapName);
    }

    public boolean doesGameMapExist(String mapName) {
        return gameMaps.containsKey(mapName);
    }

    public boolean doesLocationExist(Location location) {
        if (!doesGameMapExist(location.getMapName())) return false;
        return !gameMaps.get(location.getMapName()).isOutOfBounds(location.getX(), location.getY());
    }
}
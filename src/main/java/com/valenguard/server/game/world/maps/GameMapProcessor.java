package com.valenguard.server.game.world.maps;

import com.valenguard.server.database.sql.GameWorldNpcSQL;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.io.FilePaths;
import com.valenguard.server.io.TmxFileParser;
import com.valenguard.server.util.Log;
import lombok.Getter;

import java.io.File;
import java.util.*;

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

    public void queueAiEntitySpawn(AiEntity aiEntity) {
        aiEntitySpawnQueue.add(aiEntity);
    }

    public void queueStationaryEntitySpawn(StationaryEntity stationaryEntity) {
        stationaryEntitySpawnQueue.add(stationaryEntity);
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
            gameMaps.put(mapName, TmxFileParser.parseGameMap(FilePaths.MAPS.getFilePath(), mapName));
        }

        Log.println(getClass(), "Tmx Maps Loaded: " + files.length);
        fixWarpHeights();
    }

    public void getNpcFromDatabase() {
        for (String mapName : gameMaps.keySet()) {

            GameWorldNpcSQL gameWorldNpcSQL = new GameWorldNpcSQL();
            List<Integer> npcIdList = gameWorldNpcSQL.searchNPC(mapName);

            for (Integer i : npcIdList) {
                NPC npc = new NPC();
                npc.setFacingDirection(MoveDirection.SOUTH);
                npc.setEntityType(EntityType.NPC);
                npc.setAppearance(new Appearance(npc));
                npc.setDatabaseId(i);

                gameWorldNpcSQL.loadSQL(npc);

                queueAiEntitySpawn(npc);
            }
        }
    }

    private void fixWarpHeights() {
        for (GameMap gameMap : gameMaps.values()) {
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

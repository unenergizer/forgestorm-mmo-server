package com.valenguard.server.maps;

import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Warp;
import com.valenguard.server.maps.file.TmxFileParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MapManager {

    private static final String MAP_DIRECTORY = "src/main/resources/maps/";
    //private static final String MAP_DIRECTORY = "maps/";

    private final Map<String, TmxMap> tmxMaps = new HashMap<>();

    public MapManager() {
        loadAllMaps();
    }

    /**
     * This will dynamically load all TMX maps for the game.
     *
     * @throws RuntimeException No maps were found.
     */
    private void loadAllMaps() {
        // Find all our maps.
        File dir = new File(MAP_DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".tmx"));

        System.out.println(dir.getAbsolutePath());

        // Check to make sure we have some maps
        if (files == null) {
            throw new RuntimeException("No game maps were loaded.");
        }

        // Now load all of our maps
        for (File file : files) {
            String mapName = file.getName();
            tmxMaps.put(
                    mapName.replace(".tmx", ""),
                    TmxFileParser.loadXMLFile(MAP_DIRECTORY, mapName));
        }
        System.out.println("[TMX MAPS] Loaded: " + files.length);

        fixWarpHeights();
    }

    private void fixWarpHeights() {
        tmxMaps.values().forEach(tmxMap -> {
            for (int i = 0; i < tmxMap.getMapWidth(); i++) {
                for (int j = 0; j < tmxMap.getMapHeight(); j++) {
                    if (tmxMap.isOutOfBounds(i, j)) continue;
                    Warp warp = tmxMap.getMap()[i][j].getWarp();
                    if (warp == null) continue;
                    warp.setToY(tmxMaps.get(warp.getMapName()).getMapHeight() - warp.getToY() - 1);
                }
            }
        });
    }


    /**
     * Gets the map data associated with a map name. The map name is determined by
     * the file name of the TMX map file.
     *
     * @param mapName The name of the TMX map.
     * @return TmxMap that contains information about this map.
     * @throws RuntimeException Requested map could not be found or was not loaded.
     */
    public TmxMap getTmxMap(String mapName) throws RuntimeException {
        if (tmxMaps.containsKey(mapName)) {
            return tmxMaps.get(mapName);
        }
        throw new RuntimeException("Tried to get the map " + mapName + ", but it doesn't exist or was not loaded.");
    }
}

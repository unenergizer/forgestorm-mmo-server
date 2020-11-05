package com.forgestorm.server.io;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.maps.GameMap;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.Warp;
import com.forgestorm.server.game.world.tile.TileImage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class JsonMapParser {

    public static GameMap load(String file) {

        InputStream inputStream = JsonMapParser.class.getResourceAsStream(FilePaths.MAPS.getFilePath() + file);

        println(JsonMapParser.class, "File: " + file);
        println(JsonMapParser.class, "InputStream: " + inputStream);

        JsonValue root = new JsonReader().parse(inputStream);
        String mapName = file.replace(".json", "");
        int mapWidth = root.get("mapWidth").asInt();
        int mapHeight = root.get("mapHeight").asInt();

        Map<Integer, TileImage[]> layers = new HashMap<Integer, TileImage[]>();

        TileImage[] layer = readLayer("layer1", root, mapWidth, mapHeight);

        layers.put(0, layer);

        GameMap gameMap = new GameMap(
                mapName,
                mapWidth,
                mapHeight,
                layers
        );

        JsonValue warpsArray = root.get("warps");
        for (JsonValue jsonWarp = warpsArray.child; jsonWarp != null; jsonWarp = jsonWarp.next) {
            Warp warp = new Warp(
                    new Location(jsonWarp.get("toMap").asString(), jsonWarp.get("toX").asShort(), jsonWarp.get("toY").asShort()),
                    MoveDirection.valueOf(jsonWarp.get("facingDirection").asString())
            );
            gameMap.addTileWarp(jsonWarp.get("x").asShort(), jsonWarp.get("y").asShort(), warp);
        }

        return gameMap;
    }

    private static TileImage[] readLayer(String layerName, JsonValue root, int mapWidth, int mapHeight) {
        String layer = root.get(layerName).asString();
        String[] imageIds = layer.split(",");
        Map<Integer, TileImage> tileImages = ServerMain.getInstance().getWorldBuilder().getTileImageMap();
        TileImage[] tiles = new TileImage[mapWidth * mapHeight];
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                TileImage tileImage = tileImages.get(Integer.parseInt(imageIds[x + y * mapWidth]));
                tiles[x + y * mapWidth] = tileImage;
            }
        }
        return tiles;
    }


}

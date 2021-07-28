package com.forgestorm.server.io.todo;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.Warp;
import com.forgestorm.server.game.world.maps.WorldChunk;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.maps.building.WorldBuilder;
import com.forgestorm.server.game.world.tile.Tile;
import com.forgestorm.server.game.world.tile.TileImage;
import lombok.Getter;
import lombok.Setter;

import static com.forgestorm.server.util.Log.println;

public class ChunkLoader extends AsynchronousAssetLoader<ChunkLoader.WorldChunkDataWrapper, ChunkLoader.MapChunkParameter> {

    static class MapChunkParameter extends AssetLoaderParameters<WorldChunkDataWrapper> {
    }

    private static final boolean PRINT_DEBUG = false;
    private static final String EXTENSION_TYPE = ".json";

    private final String worldName;

    private WorldChunkDataWrapper worldChunkDataWrapper = null;

    ChunkLoader(FileHandleResolver resolver, String worldName) {
        super(resolver);
        this.worldName = worldName;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, MapChunkParameter parameter) {
        println(getClass(), "File Location: " + file.path(), false, PRINT_DEBUG);
        println(getClass(), "Is Directory: " + file.isDirectory(), false, PRINT_DEBUG);
        println(getClass(), "Directory List Size: " + file.list().length, false, PRINT_DEBUG);
        println(getClass(), "Directory Name: " + file.name(), false, PRINT_DEBUG);
        println(PRINT_DEBUG);

        worldChunkDataWrapper = null;
        worldChunkDataWrapper = new WorldChunkDataWrapper();
        WorldChunk worldChunk = parseChunk(file);
        worldChunkDataWrapper.setWorldChunkFromDisk(worldChunk);
    }

    @Override
    public WorldChunkDataWrapper loadSync(AssetManager manager, String fileName, FileHandle file, MapChunkParameter parameter) {
        return worldChunkDataWrapper;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, MapChunkParameter parameter) {
        return null;
    }

    private WorldChunk parseChunk(FileHandle fileHandle) {
        JsonValue root = new JsonReader().parse(fileHandle.reader());

        String chunkName = fileHandle.name().replace(EXTENSION_TYPE, "");
        String[] parts = chunkName.split("\\.");
        short chunkX = Short.parseShort(parts[0]);
        short chunkY = Short.parseShort(parts[1]);
        WorldChunk chunk = new WorldChunk(worldName, chunkX, chunkY);

        // Process Tile Layers
        for (LayerDefinition layerDefinition : LayerDefinition.values()) {
            readLayer(layerDefinition, root, chunk);
        }

        // Process Tile Warps
        JsonValue warpsArray = root.get("warps");
        if (warpsArray != null) {
            for (JsonValue jsonWarp = warpsArray.child; jsonWarp != null; jsonWarp = jsonWarp.next) {
                Warp warp = new Warp(
                        new Location(jsonWarp.get("toMap").asString(), jsonWarp.get("toX").asShort(), jsonWarp.get("toY").asShort()),
                        MoveDirection.valueOf(jsonWarp.get("facingDirection").asString())
                );
                chunk.addTileWarp(jsonWarp.get("x").asShort(), jsonWarp.get("y").asShort(), warp);
            }
        }

        return chunk;
    }

    private static void readLayer(LayerDefinition layerDefinition, JsonValue root, WorldChunk chunk) {

        if (root.has(layerDefinition.getLayerName())) {
            WorldBuilder worldBuilder = ServerMain.getInstance().getWorldBuilder();
            String layer = root.get(layerDefinition.getLayerName()).asString();
            String[] imageIds = layer.split(",");
            for (int localY = 0; localY < GameConstants.CHUNK_SIZE; localY++) {
                for (int localX = 0; localX < GameConstants.CHUNK_SIZE; localX++) {

                    println(ChunkLoader.class, "Processing Tile: " + layerDefinition + " - " + localX + "/" + localY, false, PRINT_DEBUG);
                    // Get the TileImage
                    int tileId = Integer.parseInt(imageIds[localX + localY * GameConstants.CHUNK_SIZE]);
                    TileImage tileImage = worldBuilder.getTileImage(tileId);

                    // Set the TileImage to the Tile
                    if (tileImage != null) {
                        println(ChunkLoader.class, " -- Setting TileImage: " + tileImage.getFileName(), false, PRINT_DEBUG);
                        Tile tile = chunk.getTile(layerDefinition, localX, localY);
                        tile.setTileImage(tileImage);
                    }
                }
            }
        }
    }

    @Setter
    @Getter
    public static class WorldChunkDataWrapper {
        private WorldChunk worldChunkFromDisk;
    }
}

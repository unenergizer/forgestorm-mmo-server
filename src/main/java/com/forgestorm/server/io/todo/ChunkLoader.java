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
import com.forgestorm.server.game.world.tile.TileImage;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class ChunkLoader extends AsynchronousAssetLoader<ChunkLoader.WorldChunkDataWrapper, ChunkLoader.WorldChunkParameter> {

    static class WorldChunkParameter extends AssetLoaderParameters<WorldChunkDataWrapper> {
    }

    private static final boolean PRINT_DEBUG = false;
    private static final String EXTENSION_TYPE = ".json";
    private WorldChunkDataWrapper worldChunkDataWrapper = null;

    ChunkLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, WorldChunkParameter parameter) {
        worldChunkDataWrapper = null;
        worldChunkDataWrapper = new WorldChunkDataWrapper();
        WorldChunk worldChunk = parseChunk(file);
        worldChunkDataWrapper.setWorldChunk(worldChunk);
        println(getClass(), "Loading chunk finished. " + worldChunk.toString(), false, PRINT_DEBUG);
    }

    @Override
    public WorldChunkDataWrapper loadSync(AssetManager manager, String fileName, FileHandle file, WorldChunkParameter parameter) {
        return worldChunkDataWrapper;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, WorldChunkParameter parameter) {
        return null;
    }

    private WorldChunk parseChunk(FileHandle fileHandle) {
        JsonValue root = new JsonReader().parse(fileHandle.reader());

        String chunkName = fileHandle.name().replace(EXTENSION_TYPE, "");
        String[] parts = chunkName.split("\\.");
        short chunkX = Short.parseShort(parts[0]);
        short chunkY = Short.parseShort(parts[1]);
        WorldChunk chunk = new WorldChunk(chunkX, chunkY);

        for (LayerDefinition layerDefinition : LayerDefinition.values()) {
            TileImage[] layer = readLayer(layerDefinition.getLayerName(), root);

            // Individually add each TileImage to the chunk (NPE FIX)
            for (int i = 0; i < layer.length; i++) {
                chunk.setTileImage(layerDefinition, layer[i], i);
            }
        }

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

    @SuppressWarnings("SameParameterValue")
    private static TileImage[] readLayer(String layerName, JsonValue root) {

        if (root.has(layerName)) {
            String layer = root.get(layerName).asString();
            String[] imageIds = layer.split(",");
            Map<Integer, TileImage> tileImages = ServerMain.getInstance().getFileManager().getTilePropertiesData().getWorldImageMap();
            TileImage[] tiles = new TileImage[GameConstants.CHUNK_SIZE * GameConstants.CHUNK_SIZE];
            for (int y = 0; y < GameConstants.CHUNK_SIZE; y++) {
                for (int x = 0; x < GameConstants.CHUNK_SIZE; x++) {
                    TileImage tileImage = tileImages.get(Integer.parseInt(imageIds[x + y * GameConstants.CHUNK_SIZE]));
                    tiles[x + y * GameConstants.CHUNK_SIZE] = tileImage;
                }
            }
            return tiles;
        } else {
            return new TileImage[GameConstants.CHUNK_SIZE * GameConstants.CHUNK_SIZE];
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @Setter
    @Getter
    public class WorldChunkDataWrapper {
        private WorldChunk worldChunk;
    }
}

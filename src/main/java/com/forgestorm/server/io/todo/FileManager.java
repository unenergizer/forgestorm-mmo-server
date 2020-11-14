package com.forgestorm.server.io.todo;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.files.FileHandle;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.io.DatabaseSettingsLoader;
import com.forgestorm.server.io.FilePaths;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

public class FileManager {

    private static final boolean PRINT_DEBUG = true;

    @Getter
    private AssetManager assetManager = new AssetManager();
    private CustomResolver filePathResolver = new CustomResolver();

    /**
     * Wrapper method to dispose of all assets. Free's system resources.
     */
    public void dispose() {
        assetManager.dispose();
    }

    /**
     * Check to see if the AssetManager has loaded a file.
     *
     * @param filePath The file to check for.
     * @return True if loaded, false if otherwise.
     */
    private boolean isFileLoaded(String filePath) {
        return assetManager.isLoaded(filePath);
    }

    /**
     * Unloads a game asset if it has already been loaded by the AssetManager.
     *
     * @param filePath The asset to try to unload.
     */
    public void unloadAsset(String filePath) {
        if (isFileLoaded(filePath)) {
            assetManager.unload(filePath);
        } else {
            println(getClass(), "Asset " + filePath + " not loaded. Nothing to unload.", true, PRINT_DEBUG);
        }
    }

    public void loadItemStackData() {
        String filePath = FilePaths.ITEM_STACK.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "ItemStackData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(ItemStackLoader.ItemStackData.class, new ItemStackLoader(filePathResolver));
            assetManager.load(filePath, ItemStackLoader.ItemStackData.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "ItemStackData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public ItemStackLoader.ItemStackData getItemStackData() {
        String filePath = FilePaths.ITEM_STACK.getFilePath();
        ItemStackLoader.ItemStackData data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, ItemStackLoader.ItemStackData.class);
        } else {
            println(getClass(), "ItemStackData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadFactionData() {
        String filePath = FilePaths.FACTIONS.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "FactionData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(FactionLoader.FactionDataWrapper.class, new FactionLoader(filePathResolver));
            assetManager.load(filePath, FactionLoader.FactionDataWrapper.class);
            assetManager.finishLoading();
            System.out.println("!GOOD LOADED!");
        } else {
            println(getClass(), "FactionData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public FactionLoader.FactionDataWrapper getFactionData() {
        String filePath = FilePaths.FACTIONS.getFilePath();
        FactionLoader.FactionDataWrapper data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, FactionLoader.FactionDataWrapper.class);
        } else {
            println(getClass(), "FactionData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadAbilityData() {
        String filePath = FilePaths.COMBAT_ABILITIES.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "AbilityData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(AbilityLoader.AbilityDataWrapper.class, new AbilityLoader(filePathResolver));
            assetManager.load(filePath, AbilityLoader.AbilityDataWrapper.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "AbilityData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public AbilityLoader.AbilityDataWrapper getAbilityData() {
        String filePath = FilePaths.COMBAT_ABILITIES.getFilePath();
        AbilityLoader.AbilityDataWrapper data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, AbilityLoader.AbilityDataWrapper.class);
        } else {
            println(getClass(), "AbilityData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    // TODO: REIMPLEMENT THESE... :)
//    public void loadEntityShopData() {
//        String filePath = FilePaths.ENTITY_SHOP.getFilePath();
//
//        // check if already loaded
//        if (isFileLoaded(filePath)) {
//            println(getClass(), "EntityShopData already loaded: " + filePath, true, PRINT_DEBUG);
//            return;
//        }
//
//        // load asset
//        if (filePathResolver.resolve(filePath).exists()) {
//            assetManager.setLoader(EntityShopLoader.EntityShopDataWrapper.class, new EntityShopLoader(filePathResolver));
//            assetManager.load(filePath, EntityShopLoader.EntityShopDataWrapper.class);
//        } else {
//            println(getClass(), "EntityShopData doesn't exist: " + filePath, true, PRINT_DEBUG);
//        }
//    }
//
//    public EntityShopLoader.EntityShopDataWrapper getEntityShopData() {
//        String filePath = FilePaths.ENTITY_SHOP.getFilePath();
//        EntityShopLoader.EntityShopDataWrapper data = null;
//
//        if (assetManager.isLoaded(filePath)) {
//            data = assetManager.get(filePath, EntityShopLoader.EntityShopDataWrapper.class);
//        } else {
//            println(getClass(), "EntityShopData not loaded: " + filePath, true, PRINT_DEBUG);
//        }
//
//        return data;
//    }

    public void loadTilePropertiesData() {
        String filePath = FilePaths.TILE_PROPERTIES.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "TilePropertiesData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(TilePropertiesLoader.TilePropertiesDataWrapper.class, new TilePropertiesLoader(filePathResolver));
            assetManager.load(filePath, TilePropertiesLoader.TilePropertiesDataWrapper.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "TilePropertiesData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public TilePropertiesLoader.TilePropertiesDataWrapper getTilePropertiesData() {
        String filePath = FilePaths.TILE_PROPERTIES.getFilePath();
        TilePropertiesLoader.TilePropertiesDataWrapper data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, TilePropertiesLoader.TilePropertiesDataWrapper.class);
        } else {
            println(getClass(), "TilePropertiesData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadNetworkSettingsData() {
        String filePath = FilePaths.NETWORK_SETTINGS.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "NetworkSettingsData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(NetworkSettingsLoader.NetworkSettingsData.class, new NetworkSettingsLoader(filePathResolver));
            assetManager.load(filePath, NetworkSettingsLoader.NetworkSettingsData.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "NetworkSettingsData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public NetworkSettingsLoader.NetworkSettingsData getNetworkSettingsData() {
        String filePath = FilePaths.NETWORK_SETTINGS.getFilePath();
        NetworkSettingsLoader.NetworkSettingsData data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, NetworkSettingsLoader.NetworkSettingsData.class);
        } else {
            println(getClass(), "NetworkSettingsData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadDatabaseSettingsData() {
        String filePath = FilePaths.DATABASE_SETTINGS.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "DatabaseSettingsData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(DatabaseSettingsLoader.DatabaseSettingsData.class, new DatabaseSettingsLoader(filePathResolver));
            assetManager.load(filePath, DatabaseSettingsLoader.DatabaseSettingsData.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "DatabaseSettingsData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public DatabaseSettingsLoader.DatabaseSettingsData getDatabaseSettingsData() {
        String filePath = FilePaths.DATABASE_SETTINGS.getFilePath();
        DatabaseSettingsLoader.DatabaseSettingsData data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, DatabaseSettingsLoader.DatabaseSettingsData.class);
        } else {
            println(getClass(), "DatabaseSettingsData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadGameWorldData() {
        String filePath = FilePaths.WORLDS_IMPL.getFilePath();

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "GameWorldData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(GameWorldLoader.GameWorldDataWrapper.class, new GameWorldLoader(filePathResolver));
            assetManager.load(filePath, GameWorldLoader.GameWorldDataWrapper.class);
            assetManager.finishLoading();
        } else {
            println(getClass(), "GameWorldData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public GameWorldLoader.GameWorldDataWrapper getGameWorldData() {
        String filePath = FilePaths.WORLDS_IMPL.getFilePath();
        GameWorldLoader.GameWorldDataWrapper data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, GameWorldLoader.GameWorldDataWrapper.class);
        } else {
            println(getClass(), "GameWorldData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void loadWorldChunkData(String worldName, short chunkX, short chunkY, boolean forceFinishLoading) {
        String filePath = FilePaths.WORLDS_IMPL.getFilePath() + "/" + worldName + "/" + chunkX + "." + chunkY + ".json";

        // check if already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "WorldChunkData already loaded: " + filePath, true, PRINT_DEBUG);
            return;
        }

        // load asset
        if (filePathResolver.resolve(filePath).exists()) {
            assetManager.setLoader(ChunkLoader.WorldChunkDataWrapper.class, new ChunkLoader(filePathResolver));
            assetManager.load(filePath, ChunkLoader.WorldChunkDataWrapper.class);
            if (forceFinishLoading) assetManager.finishLoading();
        } else {
            println(getClass(), "WorldChunkData doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
    }

    public ChunkLoader.WorldChunkDataWrapper getWorldChunkData(String worldName, short chunkX, short chunkY) {
        String filePath = FilePaths.WORLDS_IMPL.getFilePath() + "/" + worldName + "/" + chunkX + "." + chunkY + ".json";
        ChunkLoader.WorldChunkDataWrapper data = null;

        if (assetManager.isLoaded(filePath)) {
            data = assetManager.get(filePath, ChunkLoader.WorldChunkDataWrapper.class);
        } else {
            println(getClass(), "WorldChunkData not loaded: " + filePath, true, PRINT_DEBUG);
        }

        return data;
    }

    public void unloadWorldChunkData(String worldName, short chunkX, short chunkY) {
        String filePath = FilePaths.WORLDS_IMPL.getFilePath() + "/" + worldName + "/" + chunkX + "." + chunkY + ".json";
        assetManager.unload(filePath);
        if (assetManager.isLoaded(filePath)) {
            println(getClass(), "WorldChunkData was not unloaded: " + filePath, true, PRINT_DEBUG);
        } else {
            println(getClass(), "WorldChunkData was unloaded successfully! FilePath: " + filePath, true, PRINT_DEBUG);
        }
    }

    static class CustomResolver implements FileHandleResolver {

        // TODO: DO THIS THE "RIGHT" WAY...

        HeadlessFiles headlessFiles = new HeadlessFiles();

        @Override
        public FileHandle resolve(String fileName) {
            if (ServerMain.ideRun) {
                return headlessFiles.internal("src/main/resources" + fileName);
            } else {
                println(getClass(), "FileName: " + fileName);
                return headlessFiles.internal(fileName.substring(1));
            }
        }
    }

}

package com.forgestorm.server.io.todo;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.files.FileHandle;
import com.forgestorm.server.ServerMain;
import com.forgestorm.server.io.DatabaseSettingsLoader;
import com.forgestorm.server.io.FilePaths;
import com.forgestorm.server.io.TextureAtlasLoader;
import com.forgestorm.server.io.atlas.TextureAtlas;
import com.forgestorm.shared.io.*;
import com.forgestorm.shared.io.type.GameAtlas;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static com.forgestorm.server.util.Log.println;

public class FileManager {

    private static final boolean PRINT_DEBUG = false;

    @Getter
    private final String clientFilesDirectory;
    @Getter
    private final String worldDirectory;

    @Getter
    private final AssetManager assetManager = new AssetManager();
    private final HeadlessFiles headlessFiles = new HeadlessFiles();
    private final InternalResolver internalResolver = new InternalResolver();
    private final AbsoluteResolver absoluteResolver = new AbsoluteResolver();

    public FileManager() {
        // Get the path of the jar file.
        String jarPath = FileManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        println(getClass(), jarPath);
        String decodedPath = "";
        try {
            decodedPath = URLDecoder.decode(jarPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Create the client files directory
        File clientFilesDirectory = new File(decodedPath + "clientFiles");
        if (!clientFilesDirectory.exists()) {
            if (clientFilesDirectory.mkdir()) {
                println(getClass(), "Created the ClientFilesOnly directory!", true);
            } else {
                throw new RuntimeException("Couldn't create the ClientFilesOnly directory!");
            }
        }
        this.clientFilesDirectory = clientFilesDirectory.getAbsolutePath();

        // Create the World Directory if it doesn't exist.
        File worldDirectory = new File(clientFilesDirectory + "/worldDirectory");
        if (!worldDirectory.exists()) {
            if (worldDirectory.mkdir()) {
                println(getClass(), "The World directory didn't exist so one was created.", true);
            } else {
                throw new RuntimeException("Couldn't create the World directory!");
            }
        }

        this.worldDirectory = worldDirectory.getAbsolutePath();
    }

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

    public void loadAtlas(GameAtlas gameAtlas) {
        abstractedLoad(gameAtlas.getFilePath(), true, false, TextureAtlas.class, new TextureAtlasLoader(internalResolver));
    }

    public TextureAtlas getAtlas(GameAtlas gameAtlas) {
        return abstractGet(gameAtlas.getFilePath(), false, TextureAtlas.class);
    }

    public void loadItemStackData() {
        abstractedLoad(FilePaths.ITEM_STACK.getFilePath(), true, false, ItemStackLoader.ItemStackData.class, new ItemStackLoader(internalResolver));
    }

    public ItemStackLoader.ItemStackData getItemStackData() {
        return abstractGet(FilePaths.ITEM_STACK.getFilePath(), false, ItemStackLoader.ItemStackData.class);
    }

    public void loadFactionData() {
        abstractedLoad(FilePaths.FACTIONS.getFilePath(), true, false, FactionLoader.FactionDataWrapper.class, new FactionLoader(internalResolver));
    }

    public FactionLoader.FactionDataWrapper getFactionData() {
        return abstractGet(FilePaths.FACTIONS.getFilePath(), false, FactionLoader.FactionDataWrapper.class);
    }

    public void loadAbilityData() {
        abstractedLoad(FilePaths.COMBAT_ABILITIES.getFilePath(), true, false, AbilityLoader.AbilityDataWrapper.class, new AbilityLoader(internalResolver));
    }

    public AbilityLoader.AbilityDataWrapper getAbilityData() {
        return abstractGet(FilePaths.COMBAT_ABILITIES.getFilePath(), false, AbilityLoader.AbilityDataWrapper.class);
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
        abstractedLoad(FilePaths.TILE_PROPERTIES.getFilePath(), true, false, TilePropertiesLoader.TilePropertiesDataWrapper.class, new TilePropertiesLoader(internalResolver));
    }

    public TilePropertiesLoader.TilePropertiesDataWrapper getTilePropertiesData() {
        return abstractGet(FilePaths.TILE_PROPERTIES.getFilePath(), false, TilePropertiesLoader.TilePropertiesDataWrapper.class);
    }

    public void loadNetworkSettingsData() {
        abstractedLoad(FilePaths.NETWORK_SETTINGS.getFilePath(), true, false, NetworkSettingsLoader.NetworkSettingsData.class, new NetworkSettingsLoader(internalResolver));
    }

    public NetworkSettingsLoader.NetworkSettingsData getNetworkSettingsData() {
        return abstractGet(FilePaths.NETWORK_SETTINGS.getFilePath(), false, NetworkSettingsLoader.NetworkSettingsData.class);
    }

    public void loadDatabaseSettingsData() {
        abstractedLoad(FilePaths.DATABASE_SETTINGS.getFilePath(), true, false, DatabaseSettingsLoader.DatabaseSettingsData.class, new DatabaseSettingsLoader(internalResolver));
    }

    public DatabaseSettingsLoader.DatabaseSettingsData getDatabaseSettingsData() {
        return abstractGet(FilePaths.DATABASE_SETTINGS.getFilePath(), false, DatabaseSettingsLoader.DatabaseSettingsData.class);
    }

    public String loadGameWorldData(File gameWorldPath) {
        return abstractedLoad(gameWorldPath, true, true, GameWorldLoader.GameWorldDataWrapper.class, new GameWorldLoader(absoluteResolver));
    }

    public GameWorldLoader.GameWorldDataWrapper getGameWorldData(String path) {
        return abstractGet(path, true, GameWorldLoader.GameWorldDataWrapper.class);
    }

    public String loadWorldChunkData(File chunkFile, boolean forceFinishLoading, String worldName) {
        return abstractedLoad(chunkFile, forceFinishLoading, true, ChunkLoader.WorldChunkDataWrapper.class, new ChunkLoader(absoluteResolver, worldName));
    }

    public ChunkLoader.WorldChunkDataWrapper getWorldChunkData(String path) {
        return abstractGet(path, true, ChunkLoader.WorldChunkDataWrapper.class);
    }

    @SuppressWarnings("SameParameterValue")
    private <T, P extends AssetLoaderParameters<T>> String abstractedLoad(File file, boolean forceFinishLoading, boolean useAbsolutePath, Class<T> type, AssetLoader<T, P> loader) {
        String path;
        if (useAbsolutePath) {
            path = getCanonicalPath(file);
        } else {
            path = file.getPath().replace("\\", "/");
        }

        return abstractedLoad(path, forceFinishLoading, useAbsolutePath, type, loader);
    }

    private <T, P extends AssetLoaderParameters<T>> String abstractedLoad(String filePath, boolean forceFinishLoading, boolean useAbsolutePath, Class<T> type, AssetLoader<T, P> loader) {
        FileHandleResolver fileHandleResolver;

        if (useAbsolutePath) {
            fileHandleResolver = absoluteResolver;
        } else {
            fileHandleResolver = internalResolver;
        }

        // Check if the file is already loaded
        if (isFileLoaded(filePath)) {
            println(getClass(), "File already loaded: " + filePath, true, PRINT_DEBUG);
            return null;
        }

        // Load the asset
        if (fileHandleResolver.resolve(filePath).exists()) {
            assetManager.setLoader(type, loader);
            assetManager.load(filePath, type);
            if (forceFinishLoading) assetManager.finishLoading();
        } else {
            println(getClass(), "File doesn't exist: " + filePath, true, PRINT_DEBUG);
        }
        return filePath;
    }

    private <T> T abstractGet(String path, boolean useAbsolutePath, Class<T> type) {
        String pathFixed = path;

        if (useAbsolutePath) {
            // Fixes problems with loading files from an "absolute path"...
            pathFixed = path.replace("\\", "/");
        }

        if (assetManager.isLoaded(pathFixed)) {
            return assetManager.get(pathFixed, type);
        } else {
            println(getClass(), "File not loaded: " + pathFixed, true, PRINT_DEBUG);
            return null;
        }
    }

    private String getCanonicalPath(File file) {
        String path = null;
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    class InternalResolver implements FileHandleResolver {
        @Override
        public FileHandle resolve(String fileName) {
            if (ServerMain.ideRun) {
                return headlessFiles.internal("src/main/resources" + fileName);
            } else {
                println(getClass(), "FileName: " + fileName, false, PRINT_DEBUG);
                return headlessFiles.internal(fileName.substring(1));
            }
        }
    }

    class AbsoluteResolver implements FileHandleResolver {
        @Override
        public FileHandle resolve(String fileName) {
            return headlessFiles.absolute(fileName);
        }
    }

}

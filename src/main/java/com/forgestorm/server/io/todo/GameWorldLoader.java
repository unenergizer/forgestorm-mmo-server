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
import com.forgestorm.server.game.world.maps.GameWorld;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.forgestorm.server.util.Log.println;

public class GameWorldLoader extends AsynchronousAssetLoader<GameWorldLoader.GameWorldDataWrapper, GameWorldLoader.GameWorldParameter> {

    static class GameWorldParameter extends AssetLoaderParameters<GameWorldDataWrapper> {
    }

    private static final boolean PRINT_DEBUG = false;
    private static final String EXTENSION_TYPE = ".json";
    private GameWorldDataWrapper gameWorldDataWrapper = null;

    GameWorldLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, GameWorldParameter parameter) {
        println(getClass(), "Is Directory: " + file.isDirectory(), false, PRINT_DEBUG);
        println(getClass(), "Directory List Size: " + file.list().length, false, PRINT_DEBUG);
        println(getClass(), "Directory Name: " + file.name(), false, PRINT_DEBUG);

        gameWorldDataWrapper = null;
        gameWorldDataWrapper = new GameWorldDataWrapper();
        gameWorldDataWrapper.setGameWorlds(new HashMap<String, GameWorld>());

        if (ServerMain.ideRun) {
            loadFromIDE(file);
        } else {
            loadDesktopJar(file);
        }
    }

    @Override
    public GameWorldDataWrapper loadSync(AssetManager manager, String fileName, FileHandle file, GameWorldParameter parameter) {
        return gameWorldDataWrapper;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, GameWorldParameter parameter) {
        return null;
    }

    private void loadDesktopJar(FileHandle file) {
        println(getClass(), "#0 LOADED FROM DESKTOP: " + file);

        Collection<String> files = ResourceList.getDirectoryResources(file.name(), EXTENSION_TYPE);

        println(getClass(), "#1 LOADED FROM DESKTOP: " + file);

        for (String fileName : files) {
//            String worldName = fileName.substring(FilePaths.MAPS.getFilePath().length() + 1);

            println(getClass(), "#2 LOADED FROM DESKTOP: " + fileName);

            FileHandle temp = new FileHandle(fileName);

            println(getClass(), "#3 I BET THIS SHIT BREAK ONE LINE UP");

            gameWorldDataWrapper.getGameWorlds().put(fileName.replace(EXTENSION_TYPE, ""), load(temp));
        }
    }

    private void loadFromIDE(FileHandle file) {
        for (FileHandle entry : file.list()) {
            // make sure were only adding game map files
            if (entry.path().endsWith(EXTENSION_TYPE)) {
                gameWorldDataWrapper.getGameWorlds().put(entry.name().replace(EXTENSION_TYPE, ""), load(entry));
            }
        }
    }

    private GameWorld load(FileHandle fileHandle) {

        println(getClass(), "LOADED FROM DESKTOP: " + fileHandle);

        JsonValue root = new JsonReader().parse(fileHandle.reader());

        String worldName = fileHandle.name().replace(".json", "");
        int widthInChunks = root.get("widthInChunks").asInt();
        int heightInChunks = root.get("heightInChunks").asInt();

        return new GameWorld(
                worldName,
                widthInChunks,
                heightInChunks);
    }

    @SuppressWarnings("WeakerAccess")
    @Setter
    @Getter
    public class GameWorldDataWrapper {
        private Map<String, GameWorld> gameWorlds = null;
    }
}
